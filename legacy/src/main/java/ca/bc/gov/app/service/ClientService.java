package ca.bc.gov.app.service;

import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.mappers.AbstractForestClientMapper;
import ca.bc.gov.app.repository.ForestClientRepository;
import io.micrometer.observation.annotation.Observed;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientService {

  private final R2dbcEntityOperations entityTemplate;
  private final ForestClientRepository repository;
  private final ClientSearchService searchService;
  private final AbstractForestClientMapper<ForestClientDto, ForestClientEntity> mapper;

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
            .filterWhen(this::locateClient)
            .flatMap(entity -> getNextClientNumber().map(entity::withClientNumber))
            .flatMap(entity -> entityTemplate
                .insert(ForestClientEntity.class)
                .using(entity)
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

  private Mono<Boolean> locateClient(
      ForestClientEntity entity
  ) {

    log.info("Searching forest client {}", entity.getName());

    if (
        entity
            .getClientTypeCode()
            .equalsIgnoreCase("I")
    ) {
      return
          searchService
              .findByIndividual(
                  entity.getLegalFirstName(),
                  entity.getClientName(),
                  entity.getBirthdate().toLocalDate(),
                  null,
                  false
              )
              .map(client -> false) // means you can't create it
              .defaultIfEmpty(true)
              .doOnNext(
                  tag -> log.info("Individual {} forest client missing? {}", entity.getName(), tag))
              .last();
    }

    return
        repository
            .findClientByIncorporationOrName(
                entity.getClientName().toUpperCase(),
                String.join(
                    StringUtils.EMPTY,
                    entity.getRegistryCompanyTypeCode().toUpperCase(),
                    entity.getCorpRegnNmbr()
                )
            )
            .map(client -> false) // means you can't create it
            .defaultIfEmpty(true)
            .doOnNext(tag ->
                log.info(
                    "Forest client {} with type {} missing? {}",
                    entity.getName(),
                    entity.getClientTypeCode(),
                    tag
                )
            )
            .last();
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

}
