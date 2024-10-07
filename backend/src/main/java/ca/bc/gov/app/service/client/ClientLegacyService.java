package ca.bc.gov.app.service.client;

import ca.bc.gov.app.dto.client.ClientListDto;
import ca.bc.gov.app.dto.legacy.AddressSearchDto;
import ca.bc.gov.app.dto.legacy.ContactSearchDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/**
 * This class is responsible for interacting with the legacy API to fetch client data. It uses the
 * WebClient to send HTTP requests to the legacy API and converts the responses into Flux of
 * ForestClientDto objects. It provides several methods to search for clients in the legacy system
 * using different search criteria.
 * <p>
 * It is annotated with @Slf4j for logging, @Service to indicate that it's a Spring service bean,
 * and @Observed for metrics.
 * <p>
 * Each method logs the search parameters and the results for debugging purposes.
 */
@Slf4j
@Service
@Observed
public class ClientLegacyService {

  private final WebClient legacyApi;

  public ClientLegacyService(@Qualifier("legacyApi") WebClient legacyApi) {
    this.legacyApi = legacyApi;
  }

  /**
   * Searches for a list of {@link ForestClientDto} in the legacy API based on the given
   * registration number and company name.
   *
   * @param registrationNumber the registration number to search for
   * @param companyName        the company name to search for
   * @param userId             the id of the user making the request
   * @param businessId         the id of the business making the request in case of bceid
   * @return a Flux of ForestClientDto objects matching the search criteria
   */
  public Flux<ForestClientDto> searchLegacy(
      String registrationNumber,
      String companyName,
      String userId,
      String businessId
  ) {

    log.info("Searching for registration number {} and company name {} in legacy",
        registrationNumber, companyName);

    return
        legacyApi
            .get()
            .uri(builder ->
                builder
                    .path("/api/search/registrationOrName")
                    .queryParamIfPresent("registrationNumber",
                        Optional
                            .ofNullable(registrationNumber)
                            .filter(StringUtils::isNotBlank)
                            .or(
                                () -> Optional
                                    .ofNullable(userId)
                                    .filter(StringUtils::isNotBlank)
                            )
                    )
                    .queryParamIfPresent("companyName", Optional.ofNullable(companyName))
                    .build(Map.of())
            )
            .exchangeToFlux(response -> response.bodyToFlux(ForestClientDto.class))
            .doOnNext(
                dto -> log.info(
                    "Found Legacy data for {} and company name {} in legacy with client number {}",
                    registrationNumber, companyName, dto.clientNumber()));
  }

  /**
   * This method is used to search for a client in the legacy system using the client's ID and last
   * name.
   *
   * @param id       The ID of the client to search for. It could be the driver's license number,
   *                 passport number or even the BCSC GUID
   * @param lastName The last name of the client to search for.
   * @return A Flux of ForestClientDto objects that match the search criteria.
   */
  public Flux<ForestClientDto> searchIdAndLastName(
      String id,
      String lastName
  ) {

    // Log the search parameters for debugging purposes
    log.info("Searching for id {} and last name {} in legacy",
        id, lastName);

    // Use the WebClient to send a GET request to the legacy API
    return
        legacyApi
            .get()
            // Build the URI for the request
            .uri(builder ->
                builder
                    .path("/api/search/idAndLastName")
                    .queryParam("clientId", id)
                    .queryParam("lastName", lastName)
                    .build(Map.of())
            )
            // Convert the response to a Flux of ForestClientDto objects
            .exchangeToFlux(response -> response.bodyToFlux(ForestClientDto.class))
            // Log the results for debugging purposes
            .doOnNext(
                dto -> log.info(
                    "Found Legacy data for id {} and last name {} in legacy with client number {}",
                    id, lastName, dto.clientNumber())
            );
  }

  /**
   * This method is used to search for an individual in the legacy system using the individual's
   * first name, last name, date of birth, and identification.
   *
   * @param firstName      The first name of the individual to search for.
   * @param lastName       The last name of the individual to search for.
   * @param dob            The date of birth of the individual to search for.
   * @param identification The identification of the individual to search for. It could be the
   *                       driver's license number, passport number, birth certificate number, etc.
   *                       It is optional.
   * @return A Flux of ForestClientDto objects that match the search criteria.
   */
  public Flux<ForestClientDto> searchIndividual(
      String firstName,
      String lastName,
      LocalDate dob,
      String identification
  ) {

    // Log the search parameters for debugging purposes
    log.info(
        "Searching for first name {} and last name {} dob {} and possibly a document {} in legacy",
        firstName, lastName, dob, identification);

    // Use the WebClient to send a GET request to the legacy API
    return
        legacyApi
            .get()
            // Build the URI for the request
            .uri(builder ->
                builder
                    .path("/api/search/individual")
                    .queryParam("firstName", firstName)
                    .queryParam("lastName", lastName)
                    .queryParam("dob", dob)
                    .queryParamIfPresent("identification", Optional.ofNullable(identification))
                    .build(Map.of())
            )
            // Convert the response to a Flux of ForestClientDto objects
            .exchangeToFlux(response -> response.bodyToFlux(ForestClientDto.class))
            // Log the results for debugging purposes
            .doOnNext(
                dto -> log.info(
                    "Found Legacy data for first name {} and last name {} in legacy with client number {}",
                    firstName, lastName, dto.clientNumber())
            );

  }

  /**
   * This method is used to search for a document in the legacy system using the document's ID type
   * and identification.
   *
   * @param idType         The type of the ID to search for. It could be the type of document like
   *                       driver's license, passport, etc.
   * @param identification The identification of the document to search for. It could be the number
   *                       or code on the document.
   * @return A Flux of ForestClientDto objects that match the search criteria.
   */
  public Flux<ForestClientDto> searchDocument(
      String idType,
      String identification
  ) {
    // Log the search parameters for debugging purposes
    log.info("Searching for id type {} and value {} in legacy",
        idType, identification);

    // Use the WebClient to send a GET request to the legacy API
    return
        legacyApi
            .get()
            // Build the URI for the request
            .uri("/api/search/id/{idType}/{identification}", idType, identification)
            // Convert the response to a Flux of ForestClientDto objects
            .exchangeToFlux(response -> response.bodyToFlux(ForestClientDto.class))
            // Log the results for debugging purposes
            .doOnNext(
                dto -> log.info(
                    "Found Legacy data for id type {} and identification {} in legacy with client number {}",
                    idType, identification, dto.clientNumber())
            );

  }

  /**
   * Searches for a list of {@link ForestClientDto} in the legacy API based on the given search type
   * and value. This method constructs a query parameter map using the provided search type and
   * value, sends a GET request to the legacy API, and converts the response into a Flux of
   * ForestClientDto objects. It also logs the search parameters and the results for debugging
   * purposes.
   *
   * @param searchType The type of search to perform (e.g., "registrationNumber", "companyName").
   * @param value      The value to search for.
   * @return A Flux of ForestClientDto objects matching the search criteria.
   */
  public Flux<ForestClientDto> searchGeneric(
      String searchType,
      String value
  ) {
    return searchGeneric(searchType, searchType, value);
  }

  public Flux<ForestClientDto> searchGeneric(
      String searchType,
      String paramName,
      String value
  ) {

    if (StringUtils.isAnyBlank(searchType, paramName, value)) {
      return Flux.empty();
    }

    Map<String, List<String>> parameters = Map.of(paramName, List.of(value));

    return searchGeneric(searchType, parameters);
  }

  public Flux<ForestClientDto> searchGeneric(
      String searchType,
      Map<String, List<String>> parameters
  ) {

    if (
        StringUtils.isBlank(searchType)
            || parameters == null
            || parameters.isEmpty()
            || parameters.values().stream().anyMatch(CollectionUtils::isEmpty)
            || parameters.values().stream().flatMap(List::stream).anyMatch(StringUtils::isBlank)
            || parameters.keySet().stream().anyMatch(StringUtils::isBlank)
    ) {
      return Flux.empty();
    }

    return
        legacyApi
            .get()
            // Build the URI for the request
            .uri(uriBuilder ->
                uriBuilder
                    .path("/api/search/" + searchType)
                    .queryParams(CollectionUtils.toMultiValueMap(parameters))
                    .build(Map.of())
            )
            // Convert the response to a Flux of ForestClientDto objects
            .exchangeToFlux(response -> response.bodyToFlux(ForestClientDto.class))
            // Log the results for debugging purposes
            .doOnNext(
                dto -> log.info(
                    "Found Legacy data for {} with {} in legacy with client number {}",
                    searchType,
                    parameters,
                    dto.clientNumber()
                )
            );

  }

  public Flux<ForestClientDto> searchLocation(AddressSearchDto dto) {
    return
        legacyApi
            .post()
            .uri("/api/search/address")
            .body(BodyInserters.fromValue(dto))
            .exchangeToFlux(response -> response.bodyToFlux(ForestClientDto.class))
            .doOnNext(
                client -> log.info("Found Legacy data for location search with client number {}",
                    client.clientNumber())
            );
  }

  public Flux<ForestClientDto> searchContact(ContactSearchDto dto) {
    return
        legacyApi
            .post()
            .uri("/api/search/contact")
            .body(BodyInserters.fromValue(dto))
            .exchangeToFlux(response -> response.bodyToFlux(ForestClientDto.class))
            .doOnNext(
                client -> log.info("Found Legacy data for contact search with client number {}",
                    client.clientNumber())
            );
  }

  public Flux<ClientListDto> fullSearch(int page, int size, String keyword) {
    log.info(
        "Searching clients by keyword {} with page {} and size {}",
        keyword,
        page,
        size
    );
    return null;
  }
  
  public Flux<ClientListDto> predictiveSearch(String keyword) {
    log.info("Searching clients by keyword {}", keyword);
    return legacyApi
      .get()
      .uri(builder -> 
            builder
            .path("/api/search/predictive")
            .queryParam("value", keyword)
            .build(Map.of()))
        .exchangeToFlux(response -> 
          response.bodyToFlux(ClientListDto.class))
        .doOnNext(dto -> 
          log.info("Found clients by keyword {}", dto.clientNumber()));
  }

}
