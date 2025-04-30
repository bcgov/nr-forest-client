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
import java.util.HashSet;
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
  
  public Flux<Pair<HistoryLogDto, Integer>> findHistoryLogsByClientNumber(
      String clientNumber, 
      Pageable page,
      List<String> sources) {

    log.info("Searching for client history with number {} and sources {}", clientNumber, sources);

    if (StringUtils.isBlank(clientNumber)) {
      return Flux.error(new MissingRequiredParameterException("clientNumber"));
    }

    List<HistorySourceEnum> selectedSources;

    if (CollectionUtils.isEmpty(sources)) {
      selectedSources = List.of(HistorySourceEnum.values());
    } 
    else {
      try {
        selectedSources = sources.stream()
            .flatMap((String s) -> Arrays.stream(s.split(",")))
            .map(String::trim)
            .map(HistorySourceEnum::fromValue)
            .collect(Collectors.toList());
      } catch (IllegalArgumentException e) {
        return Flux.error(new MissingRequiredParameterException(e.getMessage()));
      }
    }

    List<Flux<HistoryLogDto>> selectedAuditTables = new ArrayList<>();

    for (HistorySourceEnum source : selectedSources) {
      switch (source) {
        case CLIENT_INFORMATION -> selectedAuditTables.add(
            forestClientRepository.findClientInformationHistoryLogsByClientNumber(clientNumber));
        case LOCATION -> selectedAuditTables.add(
            forestClientRepository.findLocationHistoryLogsByClientNumber(clientNumber));
        case CONTACT -> selectedAuditTables.add(
            forestClientRepository.findContactHistoryLogsByClientNumber(clientNumber));
        case DBA -> selectedAuditTables.add(
            forestClientRepository.findDoingBusinessAsHistoryLogsByClientNumber(clientNumber));
        case RELATED_CLIENT -> selectedAuditTables.add(
            forestClientRepository.findRelatedClientAsHistoryLogsByClientNumber(clientNumber));
      }
    }

    return Flux
        .merge(selectedAuditTables)
        .groupBy(dto -> dto.tableName() + dto.idx())
        .flatMap(groupedFlux ->
            groupedFlux.collectList()
                .flatMap(group -> {
                  if (group.isEmpty()) return Mono.empty();

                  Set<HistoryLogDetailsDto> details = new HashSet<>();
                  Set<HistoryLogReasonsDto> reasons = new HashSet<>();

                  group.forEach(dto -> {
                    details.add(new HistoryLogDetailsDto(dto.columnName(), dto.oldValue(), dto.newValue()));
                    reasons.add(new HistoryLogReasonsDto(dto.actionCode(), dto.reason()));
                  });

                  HistoryLogDto baseDto = group.get(0);

                  HistoryLogDto combinedDto = new HistoryLogDto(
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
                      new ArrayList<>(reasons)
                  );
                  
                  return Mono.just(Pair.of(combinedDto, group.size()));
                })
        )
        .collectList()
        .flatMapMany(list -> {
          int count = list.size();

          if (count == 0) {
            log.info("No history logs found for client {}", clientNumber);
            return Flux.empty();
          }

          log.info("Total history logs found for client {}: {}", clientNumber, count);
          return Flux.fromIterable(list);
        })
        .switchIfEmpty(
          Mono.error(new NoValueFoundException("Client with number: " + clientNumber))
        )
        .doOnNext(dto -> 
          log.info("Found history logs with client number {}", clientNumber)
        );
  }

}
