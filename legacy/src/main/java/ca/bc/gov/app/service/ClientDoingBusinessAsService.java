package ca.bc.gov.app.service;

import ca.bc.gov.app.ApplicationConstants;
import ca.bc.gov.app.dto.ClientDoingBusinessAsDto;
import ca.bc.gov.app.entity.ClientDoingBusinessAsEntity;
import ca.bc.gov.app.mappers.AbstractForestClientMapper;
import ca.bc.gov.app.repository.ClientDoingBusinessAsRepository;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@Slf4j
@Observed
public class ClientDoingBusinessAsService {

  private final R2dbcEntityOperations entityTemplate;
  private final ClientDoingBusinessAsRepository repository;
  private final AbstractForestClientMapper<
                  ClientDoingBusinessAsDto, 
                  ClientDoingBusinessAsEntity> mapper;

  public Mono<String> saveAndGetIndex(ClientDoingBusinessAsDto dto) {
    return
        Mono
            .just(dto)
            .filterWhen(dbaDto ->
                locateDoingBusinessAs(
                    dbaDto.clientNumber(),
                    dbaDto.doingBusinessAsName()
                )
                    .map(forestClientLocation -> false) // means you can't create it
                    .defaultIfEmpty(true) // means you can create it
                    .doOnNext(canCreate ->
                        log.info(
                            "Can create client doing business as {} {}? {}",
                            dto.clientNumber(),
                            dto.doingBusinessAsName(),
                            canCreate
                        )
                    )
            )
            .map(mapper::toEntity)
            .flatMap(entity -> getNextDoingBusinessAs().map(entity::withId))
            .flatMap(entity -> entityTemplate
                .insert(ClientDoingBusinessAsEntity.class)
                .using(entity)
            )
            .doOnNext(forestClientContact ->
                log.info(
                    "Saved forest client doing business as {} {}",
                    forestClientContact.getClientNumber(),
                    forestClientContact.getDoingBusinessAsName()
                )
            )
            .map(ClientDoingBusinessAsEntity::getClientNumber);
  }


  public Flux<ClientDoingBusinessAsDto> search(String dbaName) {
    return repository.matchBy(dbaName)
        .doOnNext(dba -> log.info(
            "Found forest client doing business as {} {}",
            dba.getClientNumber(),
            dba.getDoingBusinessAsName()
        ))
        .map(mapper::toDto);
  }

  private Flux<ClientDoingBusinessAsEntity> locateDoingBusinessAs(
      String clientNumber,
      String businessName
  ) {
    return
        entityTemplate
            .select(
                Query
                    .query(
                        Criteria
                            .where(ApplicationConstants.CLIENT_NUMBER)
                            .is(clientNumber)
                            .and("DOING_BUSINESS_AS_NAME").is(businessName)
                    ),
                ClientDoingBusinessAsEntity.class
            )
            .doOnNext(dba -> log.info(
                    "Forest client doing business as {} {} already exists",
                    dba.getDoingBusinessAsName(),
                    dba.getClientNumber()
                )
            );
  }

  private Mono<Integer> getNextDoingBusinessAs() {
    return
        entityTemplate
            .getDatabaseClient()
            .sql("select THE.client_dba_seq.NEXTVAL from dual")
            .fetch()
            .first()
            .map(row -> row.get("NEXTVAL"))
            .map(String::valueOf)
            .map(Integer::parseInt);
  }
}
