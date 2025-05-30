package ca.bc.gov.app.service;

import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.dto.HistoryLogDetailsDto;
import ca.bc.gov.app.dto.HistoryLogDto;
import ca.bc.gov.app.dto.HistoryLogReasonsDto;
import ca.bc.gov.app.dto.HistorySourceEnum;
import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.exception.MissingRequiredParameterException;
import ca.bc.gov.app.exception.NoValueFoundException;
import ca.bc.gov.app.mappers.AbstractForestClientMapper;
import ca.bc.gov.app.repository.ForestClientRepository;
import io.micrometer.observation.annotation.Observed;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientService {

  private final R2dbcEntityOperations entityTemplate;
  private final AbstractForestClientMapper<ForestClientDto, ForestClientEntity> mapper;
  private final ForestClientRepository forestClientRepository;

  public Mono<String> saveAndGetIndex(ForestClientDto dto) {
    return
        Mono
            .just(dto)
            .filter(forestClientDto ->
                StringUtils.isBlank(forestClientDto.clientNumber())
            )
            .doOnNext(forestClientDto ->
                log.info(
                    "Saving forest client {}",
                    forestClientDto.name()
                )
            )
            .map(mapper::toEntity)
            .flatMap(entity ->
                getNextClientNumber()
                    .map(entity::withClientNumber)
                    .flatMap(numberedEntity -> entityTemplate
                        .insert(ForestClientEntity.class)
                        .using(numberedEntity)
                    )
                    .retryWhen(
                        Retry
                            .backoff(5, Duration.ofMillis(500))
                            .doBeforeRetry(retrySignal -> log.warn(
                                    "[Check #{}] Saving client {} failed. Will retry",
                                    retrySignal.totalRetries() + 2,
                                    //We add 2 because the first one happens before the retry and starts on 0
                                entity.getName()
                                )
                            )
                            .filter(org.springframework.dao.DataIntegrityViolationException.class::isInstance)
                    )
            )
            .doOnNext(forestClientContact ->
                log.info(
                    "Saved forest client {} {}",
                    forestClientContact.getClientNumber(),
                    forestClientContact.getName()
                )
            )
            .map(ForestClientEntity::getClientNumber)
            .switchIfEmpty(
                Mono
                    .justOrEmpty(
                        Optional.ofNullable(
                            dto.clientNumber()
                        )
                    )
                    .doOnNext(
                        clientNumber -> log.info("Client with number {} already exists", clientNumber)
                    )
            );
  }
  
  private Mono<String> getNextClientNumber() {
    return
        entityTemplate
            .getDatabaseClient()
            .sql("""
                UPDATE
                max_client_nmbr
                SET
                client_number =  (SELECT LPAD(TO_NUMBER(NVL(max(CLIENT_NUMBER),'0'))+1,8,'0') FROM FOREST_CLIENT)"""
            )
            .fetch()
            .rowsUpdated()
            .then(
                entityTemplate
                    .getDatabaseClient()
                    .sql("SELECT client_number FROM max_client_nmbr")
                    .map((row, rowMetadata) -> row.get("client_number", String.class))
                    .first()
            )
            .doOnNext(clientNumber -> log.info("Next client number is {}", clientNumber));
  }
  
  /**
   * Retrieves a reactive stream of client history log entries based on the provided client number,
   * paginated request, and list of source types. Each entry is returned as a pair containing a
   * consolidated {@link HistoryLogDto} and the count of records grouped into that single entry.
   *
   * <p>The method supports filtering logs from multiple source tables, defined by 
   * {@link HistorySourceEnum}.
   * If the source list is empty, it defaults to retrieving logs from all available sources.
   * Input sources are expected as comma-separated strings.
   *
   * <p>The log entries are grouped by their table name and index, and each group is consolidated
   * into a single {@code HistoryLogDto} with associated details and reasons.
   *
   * <p>Examples of history log types include client information, contact data, location data, 
   * DBA (doing-business-as) names, and related client associations.
   *
   * @param clientNumber the unique client identifier for which history logs are to be retrieved;
   *                     must not be null or blank
   * @param page the pagination information
   * @param sources a list of source identifiers (comma-separated if multiple in one string);
   *                can be empty or null to indicate all sources
   * @return a {@link Flux} emitting {@code Pair<HistoryLogDto, Integer>}, where each pair contains
   *         a grouped and consolidated history log DTO and the number of entries it was built from
   * @throws MissingRequiredParameterException if {@code clientNumber} is blank or the provided
   *         source string is invalid
   * @throws NoValueFoundException if no history logs are found for the given client number
   */
  public Flux<Pair<HistoryLogDto, Integer>> findHistoryLogsByClientNumber(
      String clientNumber, Pageable page, List<String> sources) {

    log.info("Searching for client history with number {} and sources {}", clientNumber, sources);

    if (StringUtils.isBlank(clientNumber)) {
      return Flux.error(new MissingRequiredParameterException("clientNumber"));
    }

    final List<HistorySourceEnum> selectedSources;
    if (CollectionUtils.isEmpty(sources)) {
      selectedSources = List.of(HistorySourceEnum.values());
    } else {
      try {
        selectedSources =
            sources.stream()
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .map(HistorySourceEnum::fromValue)
                .collect(Collectors.toList());
      } catch (IllegalArgumentException e) {
        return Flux.error(new MissingRequiredParameterException(e.getMessage()));
      }
    }

    final List<Flux<HistoryLogDto>> selectedAuditTables = new ArrayList<>();
    for (HistorySourceEnum source : selectedSources) {
      switch (source) {
        case CLIENT_INFORMATION ->
            selectedAuditTables.add(
                forestClientRepository.findClientInformationHistoryLogsByClientNumber(clientNumber)
            );
        case LOCATION ->
            selectedAuditTables.add(
                forestClientRepository.findLocationHistoryLogsByClientNumber(clientNumber)
            );
        case CONTACT ->
            selectedAuditTables.add(
                forestClientRepository.findContactHistoryLogsByClientNumber(clientNumber)
            );
        case DBA ->
            selectedAuditTables.add(
                forestClientRepository.findDoingBusinessAsHistoryLogsByClientNumber(clientNumber)
            );
        case RELATED_CLIENT ->
            selectedAuditTables.add(
                forestClientRepository.findRelatedClientAsHistoryLogsByClientNumber(clientNumber)
            );
        default -> {
          log.warn("Unhandled source type: {}", source);
        }
      }
    }

    return Flux.merge(selectedAuditTables)
        .groupBy(dto -> dto.tableName() + dto.idx())
        .flatMap(
            groupedFlux ->
                groupedFlux
                    .collectList()
                    .filter(group -> !group.isEmpty())
                    .map(
                        group -> {
                          final Set<HistoryLogDetailsDto> details = new LinkedHashSet<>();
                          final Set<HistoryLogReasonsDto> reasons = new LinkedHashSet<>();

                          for (HistoryLogDto dto : group) {
                            details.add(
                                new HistoryLogDetailsDto(
                                    dto.columnName(), 
                                    dto.oldValue(), 
                                    dto.newValue()));
                            reasons.add(
                                new HistoryLogReasonsDto(
                                    dto.actionCode(), 
                                    dto.reason()));
                          }

                          final HistoryLogDto baseDto = group.get(0);
                          final HistoryLogDto combinedDto =
                              new HistoryLogDto(
                                  baseDto.tableName(),
                                  baseDto.idx(),
                                  baseDto.identifierLabel(),
                                  null,
                                  null,
                                  null,
                                  baseDto.updateTimestamp(),
                                  baseDto.updateUserid(),
                                  baseDto.changeType(),
                                  null,
                                  null,
                                  new ArrayList<>(details),
                                  new ArrayList<>(reasons));

                          return Pair.of(combinedDto, group.size());
                        }))
        .collectList()
        .flatMapMany(
            list -> {      
              list.sort(
                  Comparator
                      .comparing(
                          (Pair<HistoryLogDto, Integer> pair) ->
                              "Client created".equals(pair.getLeft().identifierLabel()) ? 1 : 0
                      )
                      .thenComparing(
                          (Pair<HistoryLogDto, Integer> pair) ->
                              pair.getLeft().updateTimestamp(),
                          Comparator.reverseOrder()
                      )
              );

              log.info("Total history logs found for client {}: {}", clientNumber, list.size());
              return Flux.fromIterable(list);
            });
  }

}
