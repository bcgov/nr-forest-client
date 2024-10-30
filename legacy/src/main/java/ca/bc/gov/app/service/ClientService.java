package ca.bc.gov.app.service;

import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.mappers.AbstractForestClientMapper;
import io.micrometer.observation.annotation.Observed;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientService {

  private final R2dbcEntityOperations entityTemplate;
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
                    forestClientDto.legalName()
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

}
