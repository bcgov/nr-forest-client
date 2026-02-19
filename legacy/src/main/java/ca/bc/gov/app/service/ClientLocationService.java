package ca.bc.gov.app.service;

import ca.bc.gov.app.ApplicationConstants;
import ca.bc.gov.app.dto.ClientNameCodeDto;
import ca.bc.gov.app.dto.CodeNameDto;
import ca.bc.gov.app.dto.ForestClientLocationDto;
import ca.bc.gov.app.entity.ForestClientLocationEntity;
import ca.bc.gov.app.entity.ForestClientMailingCountryEntity;
import ca.bc.gov.app.mappers.AbstractForestClientMapper;
import ca.bc.gov.app.repository.ForestClientLocationRepository;
import ca.bc.gov.app.repository.LocationUpdateReasonRepository;
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
public class ClientLocationService {

  private final R2dbcEntityOperations entityTemplate;
  private final AbstractForestClientMapper<ForestClientLocationDto, ForestClientLocationEntity> mapper;
  private final ForestClientLocationRepository repository;
  private final LocationUpdateReasonRepository locationReason;

  public Mono<String> saveAndGetIndex(ForestClientLocationDto dto) {

    return
        //Load the country detail from the database
        getCountry(dto.country())
            .map(ClientNameCodeDto::name)
            .map(dto::withCountry)
            .defaultIfEmpty(dto) //This will only be invoked if the country is not on the list
            .filterWhen(locationDto ->
                locateClientLocation(locationDto.clientNumber(), locationDto.clientLocnCode())
                    .map(forestClientLocation -> false) // means you can't create it
                    .defaultIfEmpty(true) // means you can create it
                    .doOnNext(canCreate ->
                        log.info(
                            "Can create client location {} {}? {}",
                            locationDto.clientNumber(),
                            locationDto.clientLocnName(),
                            canCreate
                        )
                    )
            )
            .map(mapper::toEntity)
            .flatMap(entity -> entityTemplate
                .insert(ForestClientLocationEntity.class)
                .using(entity)
            )
            .doOnNext(forestClientContact ->
                log.info(
                    "Saved forest client location {} {}",
                    forestClientContact.getClientNumber(),
                    forestClientContact.getClientLocnName()
                )
            )
            .map(ForestClientLocationEntity::getClientNumber);
  }

  public Flux<ForestClientLocationDto> search(String address, String postalCode) {
    log.info("Searching for forest client location {} {}", address, postalCode);
    return repository
        .matchBy(address, postalCode)
        .doOnNext(forestClientLocation -> log
            .info("Found forest client location {} {}",
                forestClientLocation.getClientNumber(),
                forestClientLocation.getAddressOne(),
                forestClientLocation.getPostalCode()))
        .map(mapper::toDto);
  }

  public Flux<CodeNameDto> findAllLocationUpdatedWithClient(String clientNumber, String clientStatus) {
    log.info("Listing locations from client {} that changes status when client went {}",
        clientNumber, clientStatus);
    return locationReason.findAllLocationUpdatedWithClient(
        clientNumber,
        clientStatus
    );
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

  private Mono<ClientNameCodeDto> getCountry(String code) {
    return entityTemplate
        .selectOne(
            Query.query(Criteria.where("code").is(code)),
            ForestClientMailingCountryEntity.class
        )
        .map(entity -> new ClientNameCodeDto(entity.getCode(), entity.getName()))
        .doOnNext(
            country -> log.info("Found country {} for code {}", country.name(), country.code()));
  }
}
