package ca.bc.gov.app.service;

import ca.bc.gov.app.ApplicationConstants;
import ca.bc.gov.app.dto.ForestClientLocationDto;
import ca.bc.gov.app.entity.ForestClientLocationEntity;
import ca.bc.gov.app.mappers.AbstractForestClientMapper;
import ca.bc.gov.app.repository.ForestClientLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@Slf4j
public class ClientLocationService {

  private final ForestClientLocationRepository repository;
  private final R2dbcEntityOperations entityTemplate;
  private final AbstractForestClientMapper<ForestClientLocationDto, ForestClientLocationEntity> mapper;

  public Mono<String> saveAndGetIndex(ForestClientLocationDto dto) {
    return
        Mono
            .just(dto)
            .filterWhen(locationDto ->
                locateClientLocation(locationDto.clientNumber(), locationDto.clientLocnCode())
                    .map(forestClientLocation -> false) // means you can't create it
                    .defaultIfEmpty(true) // means you can create it
            )
            .map(mapper::toEntity)
            .flatMap(entity -> entityTemplate
                .insert(ForestClientLocationEntity.class)
                .using(entity)
            )
            .map(ForestClientLocationEntity::getClientNumber);
  }

  private Mono<ForestClientLocationEntity> locateClientLocation(
      String clientNumber,
      String locationCode
  ) {
    return
        entityTemplate
            .selectOne(
                Query
                    .query(
                        Criteria
                            .where("CLIENT_LOCN_CODE").is(locationCode)
                            .and(ApplicationConstants.CLIENT_NUMBER).is(clientNumber)
                    ),
                ForestClientLocationEntity.class
            )
            .doOnNext(forestClientLocation -> log.info(
                    "Forest client location {} {} already exists",
                    forestClientLocation.getClientLocnCode(),
                    forestClientLocation.getClientLocnName()
                )
            );
  }

}
