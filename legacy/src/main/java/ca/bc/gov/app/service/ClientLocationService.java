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

/**
 * Service class for managing forest client location operations.
 *
 * <p>This service provides functionality to create, search, and retrieve client location
 * information. It handles the business logic for location management including country
 * validation and duplicate detection.</p>
 *
 * @see ForestClientLocationDto
 * @see ForestClientLocationEntity
 * @see CodeNameDto
 */
@RequiredArgsConstructor
@Service
@Slf4j
@Observed
public class ClientLocationService {

  private final R2dbcEntityOperations entityTemplate;
  private final AbstractForestClientMapper<ForestClientLocationDto,
      ForestClientLocationEntity> mapper;
  private final ForestClientLocationRepository repository;
  private final LocationUpdateReasonRepository locationReason;

  /**
   * Saves a new client location and returns the client number.
   *
   * <p>This method performs the following steps:</p>
   * <ol>
   *   <li>Validates and resolves the country code to its full name</li>
   *   <li>Checks if the location already exists for the client</li>
   *   <li>If the location doesn't exist, creates a new location record</li>
   * </ol>
   *
   * @param dto the location data transfer object containing the location details
   * @return a {@link Mono} emitting the client number of the saved location,
   *         or empty if the location already exists
   */
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

  /**
   * Searches for client locations by address and postal code.
   *
   * <p>This method performs a match search against the location repository using
   * the provided address and postal code criteria.</p>
   *
   * @param address    the address to search for
   * @param postalCode the postal code to search for
   * @return a {@link Flux} emitting matching {@link ForestClientLocationDto} objects
   */
  public Flux<ForestClientLocationDto> search(String address, String postalCode) {
    log.info("Searching for forest client location {} {}", address, postalCode);
    return repository
        .matchBy(address, postalCode)
        .doOnNext(forestClientLocation -> log
            .info("Found forest client location {} {} {}",
                forestClientLocation.getClientNumber(),
                forestClientLocation.getAddressOne(),
                forestClientLocation.getPostalCode()))
        .map(mapper::toDto);
  }

  /**
   * Retrieves all locations that were updated when the client status changed.
   *
   * <p>This method finds all location records that were modified at the same timestamp
   * as the client's status change. This is useful for identifying which locations were
   * affected when a client was activated or deactivated.</p>
   *
   * @param clientNumber the unique identifier of the forest client
   * @param clientStatus the client status code (e.g., "ACT" for active,
   *                     "DAC" for deactivated)
   * @return a {@link Flux} emitting {@link CodeNameDto} objects containing location
   *         codes and names that were updated with the client
   */
  public Flux<CodeNameDto> findAllLocationUpdatedWithClient(
      String clientNumber,
      String clientStatus
  ) {
    log.info("Listing locations from client {} that changes status when client went {}",
        clientNumber, clientStatus);
    return locationReason.findAllLocationUpdatedWithClient(
        clientNumber,
        clientStatus
    );
  }

  /**
   * Locates an existing client location by client number and location code.
   *
   * @param clientNumber the unique identifier of the forest client
   * @param locationCode the location code to search for
   * @return a {@link Mono} emitting the found {@link ForestClientLocationEntity},
   *         or empty if not found
   */
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

  /**
   * Retrieves country information by its code.
   *
   * @param code the country code to look up
   * @return a {@link Mono} emitting a {@link ClientNameCodeDto} with the country
   *         code and name, or empty if not found
   */
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
