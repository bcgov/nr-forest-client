package ca.bc.gov.app.service.client;

import static ca.bc.gov.app.ApplicationConstant.MDC_USERID;
import static ca.bc.gov.app.ApplicationConstant.REQUEST_LEGACY;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.client.ClientListDto;
import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.client.RelatedClientDto;
import ca.bc.gov.app.dto.client.RelatedClientEntryDto;
import ca.bc.gov.app.dto.legacy.AddressSearchDto;
import ca.bc.gov.app.dto.legacy.ClientRelatedProjection;
import ca.bc.gov.app.dto.legacy.ContactSearchDto;
import ca.bc.gov.app.dto.legacy.ForestClientDetailsDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.dto.legacy.HistoryLogDto;
import com.fasterxml.jackson.databind.JsonNode;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This class is responsible for interacting with the legacy API to fetch client data. It uses the
 * WebClient to send HTTP requests to the legacy API and converts the responses into Flux of
 * ForestClientDto objects. It provides several methods to search for clients in the legacy system
 * using different search criteria.
 *
 * <p>It is annotated with @Slf4j for logging, @Service to indicate that it's a
 * Spring service bean, and @Observed for metrics.
 *
 * <p>Each method logs the search parameters and the results for debugging purposes.
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
            .name(REQUEST_LEGACY)
            .tag("kind", "registrationOrNameSearch")
            .doOnNext(
                dto -> log.info(
                    "Found Legacy data for {} and company name {} in legacy with client number {}",
                    registrationNumber, companyName, dto.clientNumber()));
  }

  /**
   * Searches for client details by client number using the legacy API.
   *
   * <p>This method communicates with the legacy API to retrieve client information based on the
   * provided client number. Optionally, a list of groups can be specified to refine the search
   * criteria. If a matching record is found, it is returned as a {@link ForestClientDetailsDto}.
   *
   * @param clientNumber the client number to search for
   * @return a {@link Mono} emitting the {@link ForestClientDetailsDto} if the client is found
   */
  public Mono<ForestClientDetailsDto> searchByClientNumber(String clientNumber) {
    log.info("Searching for client number {} in legacy", clientNumber);

    return
        legacyApi
            .get()
            .uri("/api/search/clientNumber/{clientNumber}", clientNumber)
            .exchangeToMono(response -> response.bodyToMono(ForestClientDetailsDto.class))
            .name(REQUEST_LEGACY)
            .tag("kind", "clientNumberSearch")
            .doOnNext(
                dto -> log.info(
                    "Found Legacy data for in legacy with client number {}",
                    dto.client().clientNumber())
            );
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
            .name(REQUEST_LEGACY)
            .tag("kind", "idAndLastNameSearch")
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
            .name(REQUEST_LEGACY)
            .tag("kind", "individualSearch")
            // Log the results for debugging purposes
            .doOnNext(dto ->
                log.info(
                    "Found data for first {} and last name {} in legacy with client number {}",
                    firstName, lastName, dto.clientNumber()
                )
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
            .name(REQUEST_LEGACY)
            .tag("kind", "idTypeIdentificationSearch")
            // Log the results for debugging purposes
            .doOnNext(
                dto -> log.info(
                    "Found data for id type {} and identification {} in legacy with client number {}",
                    idType, identification, dto.clientNumber())
            );

  }

  /**
   * Retrieves a flux of {@link CodeNameDto} objects representing the update reasons for a specific
   * client type and action code. This method queries in the legacy system to find the reasons
   * associated with updating a client based on the given parameters.
   *
   * <p>The method logs the request parameters and the results for debugging purposes.
   *
   * @param clientTypeCode the code representing the type of client (e.g., individual, corporation)
   * @param actionCode     the code representing the action being performed (e.g., name change,
   *                       address change)
   * @return a {@link Flux} emitting {@link CodeNameDto} objects that represent the update reasons
   *        for the specified client type and action code
   */
  public Flux<CodeNameDto> findActiveUpdateReasonsByClientTypeAndActionCode(
      String clientTypeCode,
      String actionCode
  ) {
    // Log the parameters for debugging purposes
    log.info("Searching for client type {} and action code {} in legacy",
        clientTypeCode,
        actionCode);

    return
        legacyApi
            .get()
            .uri("/api/codes/update-reasons/{clientTypeCode}/{actionCode}",
                clientTypeCode,
                actionCode)
            .exchangeToFlux(response -> response.bodyToFlux(CodeNameDto.class))
            .name(REQUEST_LEGACY)
            .tag("kind", "updateReasonsCodes")
            // Log the results for debugging purposes
            .doOnNext(
                dto -> log.info(
                    "Found data for client type {} and action code {} in legacy",
                    clientTypeCode,
                    actionCode
                )
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
            .name(REQUEST_LEGACY)
            .tag("kind", "searchGeneric" + searchType)
            // Log the results for debugging purposes
            .doOnNext(
                dto -> log.info(
                    "Found data for {} with {} in legacy with client number {}",
                    searchType,
                    parameters,
                    dto.clientNumber()
                )
            );

  }

  /**
   * Searches for clients in the legacy system based on the provided address details.
   *
   * @param dto The address search criteria.
   * @return A Flux of ForestClientDto objects that match the search criteria.
   */
  public Flux<ForestClientDto> searchLocation(AddressSearchDto dto) {
    return
        legacyApi
            .post()
            .uri("/api/search/address")
            .body(BodyInserters.fromValue(dto))
            .exchangeToFlux(response -> response.bodyToFlux(ForestClientDto.class))
            .name(REQUEST_LEGACY)
            .tag("kind", "addressSearch")
            .doOnNext(
                client -> log.info("Found Legacy data for location search with client number {}",
                    client.clientNumber())
            );
  }

  /**
   * Searches for clients in the legacy system based on the provided contact details.
   *
   * @param dto The contact search criteria.
   * @return A Flux of ForestClientDto objects that match the search criteria.
   */
  public Flux<ForestClientDto> searchContact(ContactSearchDto dto) {
    return
        legacyApi
            .post()
            .uri("/api/search/contact")
            .body(BodyInserters.fromValue(dto))
            .exchangeToFlux(response -> response.bodyToFlux(ForestClientDto.class))
            .name(REQUEST_LEGACY)
            .tag("kind", "contactSearch")
            .doOnNext(
                client -> log.info("Found Legacy data for contact search with client number {}",
                    client.clientNumber())
            );
  }

  /**
   * Searches for clients in the legacy system based on the provided keyword, page, and size.
   *
   * @param page    The page number to retrieve.
   * @param size    The number of records per page.
   * @param keyword The keyword to search for.
   * @return A Flux of pairs containing ClientListDto objects and the total count of matching
   * records.
   */
  public Flux<Pair<ClientListDto, Long>> search(int page, int size, String keyword) {
    log.info(
        "Searching clients by keyword {} with page {} and size {}",
        keyword,
        page,
        size
    );

    return legacyApi
        .get()
        .uri(builder ->
            builder
                .path("/api/search")
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("value", keyword)
                .build(Map.of())
        )
        .exchangeToFlux(response -> {
          List<String> totalCountHeader = response.headers().header("X-Total-Count");
          Long count = totalCountHeader.isEmpty() ? 0L : Long.valueOf(totalCountHeader.get(0));

          return response
              .bodyToFlux(ClientListDto.class)
              .map(dto -> Pair.of(dto, count));
        })
        .name(REQUEST_LEGACY)
        .tag("kind", "searchByKeyword")
        .doOnNext(pair -> {
          ClientListDto dto = pair.getFirst();
          Long totalCount = pair.getSecond();
          log.info("Found clients by keyword {}, total count: {}", dto.clientNumber(), totalCount);
        });
  }

  /**
   * Retrieves active client status codes from the legacy system.
   *
   * @return a Flux containing {@link CodeNameDto} objects representing active client statuses.
   */
  public Flux<CodeNameDto> findActiveClientStatusCodes() {
    log.info("Searching for active client statuses in legacy");

    return
        legacyApi
            .get()
            .uri("/api/codes/client-statuses")
            .exchangeToFlux(response -> response.bodyToFlux(CodeNameDto.class))
            .name(REQUEST_LEGACY)
            .tag("kind", "clientStatusActiveCodes")
            .doOnNext(
                dto -> log.info(
                    "Found active client statuses in legacy"
                )
            );
  }

  /**
   * Retrieves active client status codes filtered by client type and role from the legacy system.
   *
   * @param clientTypeCode the client type code to filter by.
   * @param groups         the set of roles to filter by.
   * @return a Flux containing filtered {@link CodeNameDto} objects.
   */
  public Flux<CodeNameDto> findActiveClientStatusCodesByClientTypeAndRole(
      String clientTypeCode, Set<String> groups) {

    log.info(
        "Searching for active client statuses in legacy by client type {} and role {}",
        clientTypeCode, groups);

    return legacyApi
        .get()
        .uri("/api/codes/client-statuses")
        .exchangeToFlux(response -> response.bodyToFlux(CodeNameDto.class))
        .name(REQUEST_LEGACY)
        .tag("kind", "clientStatusValidCodes")
        .filter(dto -> isValidClientStatus(dto, clientTypeCode, groups))
        .doOnNext(dto -> log.info("Filtered active client status: {}", dto));
  }

  /**
   * Sends a PATCH request to the legacy system to update a client with the given client number.
   *
   * @param clientNumber the client number to update
   * @param forestClient the JSON Patch document describing the modifications
   * @param userName the user that triggered the request
   * @return a {@link Mono} that completes when the patch is applied successfully
   */
  public Mono<Void> patchClient(
      String clientNumber,
      JsonNode forestClient,
      String userName
  ) {
    log.info("Sending request to the legacy system to patch client {}", clientNumber);
    return legacyApi
        .patch()
        .uri("/api/clients/partial/{clientNumber}", clientNumber)
        .contentType(MediaType.asMediaType(new MimeType("application", "json-patch+json")))
        .header(MDC_USERID, userName)
        .body(BodyInserters.fromValue(forestClient))
        .exchangeToMono(response -> {
          //if 201 is good, else already exist, so move forward
          if (response.statusCode().is2xxSuccessful()) {
            return response.bodyToMono(Void.class);
          } else {
            return response.createError();
          }
        })
        .name(REQUEST_LEGACY)
        .tag("kind", "patchClient");
  }

  /**
   * Retrieves active registry type codes from the legacy system.
   *
   * @return a Flux containing {@link CodeNameDto} objects representing active registry types.
   */
  public Flux<CodeNameDto> findActiveRegistryTypeCodes() {
    log.info("Searching for active registry types in legacy");

    return
        legacyApi
            .get()
            .uri("/api/codes/registry-types")
            .exchangeToFlux(response -> response.bodyToFlux(CodeNameDto.class))
            .name(REQUEST_LEGACY)
            .tag("kind", "registryTypeCodes")
            // Log the results for debugging purposes
            .doOnNext(
                dto -> log.info(
                    "Found active registry types in legacy"
                )
            );
  }

  public Flux<Pair<HistoryLogDto, Long>> retrieveHistoryLogs(
      String clientNumber, int page, int size, List<String> sources) {

    log.info("Retrieving history log for client {} with page {} and size {} and sources {}",
        clientNumber, page, size, sources);

    return
	    legacyApi
	        .get()
	        .uri(builder ->
	            builder
	                .path("/api/clients/history-logs/" + clientNumber)
	                .queryParam("page", page)
	                .queryParam("size", size)
	                .queryParam("sources", sources)
                    .build()
	        )
	        .exchangeToFlux(response -> {
	            List<String> totalCountHeader = response.headers().header("X-Total-Count");
	            Long count = totalCountHeader.isEmpty() ? 
	                0L : Long.valueOf(totalCountHeader.get(0));

	            return response
	                .bodyToFlux(HistoryLogDto.class)
	                .map(dto -> Pair.of(dto, count));
	         })
          .name(REQUEST_LEGACY)
          .tag("kind", "historyLog")
	        .doOnNext(
	            dto -> log.info(
	                "Found audit data in legacy system for client number {}", clientNumber
	            )
	        );
  }

  public Flux<CodeNameDto> findActiveRegistryTypeCodesByClientTypeCode(String clientTypeCode) {
    log.info("Searching for active registry types in legacy by client type {}", clientTypeCode);

    return
        legacyApi
            .get()
            .uri("/api/codes/registry-types/{clientTypeCode}", clientTypeCode)
            .exchangeToFlux(response -> response.bodyToFlux(CodeNameDto.class))
            .name(REQUEST_LEGACY)
            .tag("kind", "registryTypeCodes")
            // Log the results for debugging purposes
            .doOnNext(
                dto -> log.info(
                    "Found active registry types in legacy with client type {}",
                    clientTypeCode
                )
            );
  }

  public Flux<CodeNameDto> findActiveClientTypeCodes() {
    log.info("Searching for active client types in legacy");

    return
        legacyApi
            .get()
            .uri("/api/codes/client-types")
            .exchangeToFlux(response -> response.bodyToFlux(CodeNameDto.class))
            .name(REQUEST_LEGACY)
            .tag("kind", "clientTypeCodes")
            // Log the results for debugging purposes
            .doOnNext(
                dto -> log.info(
                    "Found active client types in legacy"
                )
            );
  }

  public Flux<CodeNameDto> findActiveIdentificationTypeCodes() {
    log.info("Searching for active client ID types in legacy");

    return
        legacyApi
            .get()
            .uri("/api/codes/client-id-types")
            .exchangeToFlux(response -> response.bodyToFlux(CodeNameDto.class))
            .name(REQUEST_LEGACY)
            .tag("kind", "clientIdTypeCodes")
            // Log the results for debugging purposes
            .doOnNext(
                dto -> log.info(
                    "Found active client ID types in legacy"
                )
            );
  }

  public Mono<Map<String, List<RelatedClientEntryDto>>> getRelatedClientList(String clientNumber) {
    log.info("Searching for related clients for relatedClient number {}", clientNumber);

    return
        legacyApi
            .get()
            .uri("/api/clients/{clientNumber}/related-clients", clientNumber)
            .exchangeToFlux(response -> response.bodyToFlux(ClientRelatedProjection.class))
            .name(REQUEST_LEGACY)
            .tag("kind", "relatedClientList")
            .map(legacyProjection -> new RelatedClientEntryDto(
                    mapCorrectClient(legacyProjection, true),
                    mapCorrectClient(legacyProjection, false),
                    new CodeNameDto(
                        legacyProjection.relationshipCode(),
                        legacyProjection.relationshipName()
                    ),
                    legacyProjection.percentOwnership(),
                    BooleanUtils.toBoolean(Objects.toString(legacyProjection.signingAuthInd(), "N"),
                        "Y", "N"),
                    legacyProjection.primaryClient()
                )
            )
            .collectList()
            .map(list -> list
                .stream()
                .collect(Collectors.groupingBy(dto -> dto.client().location().code()))
            )
            .doOnNext(
                relatedClient -> log.info(
                    "Found {} related clients for number: {}",
                    relatedClient.size(), clientNumber
                )
            );
  }

  public Flux<ClientListDto> searchRelatedClients(
      String clientNumber,
      String type,
      String value
  ) {
    return
        legacyApi
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/api/search/relation/{clientNumber}")
                    .queryParamIfPresent("type", Optional.ofNullable(type))
                    .queryParam("value", value)
                    .build(clientNumber)
            )
            .exchangeToFlux(response -> response.bodyToFlux(ClientListDto.class))
            .name(REQUEST_LEGACY)
            .tag("kind", "relationAutoCompleteSearch");
  }

  private boolean isValidClientStatus(CodeNameDto dto, String clientTypeCode, Set<String> groups) {
    if (groups.contains(ApplicationConstant.ROLE_ADMIN)) {
      return getAdminStatuses(clientTypeCode).contains(dto.code());
    } else if (groups.contains(ApplicationConstant.ROLE_EDITOR)) {
      return getEditorStatuses().contains(dto.code());
    } else if (groups.contains(ApplicationConstant.ROLE_SUSPEND)) {
      return getSuspendStatuses().contains(dto.code());
    }
    return false;
  }

  private Set<String> getAdminStatuses(String clientTypeCode) {
    return switch (clientTypeCode) {
      case "F", "G" -> Set.of("ACT", "DAC");
      case "I" -> Set.of("ACT", "DEC", "REC", "DAC", "SPN");
      default -> Set.of("ACT", "DAC", "REC", "SPN");
    };
  }

  private Set<String> getEditorStatuses() {
    return Set.of("ACT", "DAC");
  }

  private Set<String> getSuspendStatuses() {
    return Set.of("ACT", "SPN", "REC");
  }

  public Flux<CodeNameDto> findActiveRelationshipCodesByClientTypeCode(String clientTypeCode) {
    log.info("Searching for active relationship types in legacy by client type {}", clientTypeCode);

    return
        legacyApi
            .get()
            .uri("/api/codes/relationship-types/{clientTypeCode}", clientTypeCode)
            .exchangeToFlux(response -> response.bodyToFlux(CodeNameDto.class))
            .name(REQUEST_LEGACY)
            .tag("kind", "relationshipTypeCodes")
            // Log the results for debugging purposes
            .doOnNext(
                dto -> log.info(
                    "Found active relationship types in legacy with client type {}",
                    clientTypeCode
                )
            );
  }

  private RelatedClientDto mapCorrectClient(ClientRelatedProjection projection, boolean primary) {
    if (primary == BooleanUtils.isTrue(projection.primaryClient())) {
      return new RelatedClientDto(
          new CodeNameDto(projection.clientNumber(), projection.clientName()),
          new CodeNameDto(projection.clientLocnCode(), projection.clientLocnName())
      );
    }
    return new RelatedClientDto(
        new CodeNameDto(projection.relatedClntNmbr(), projection.relatedClntName()),
        new CodeNameDto(projection.relatedClntLocn(), projection.relatedClntLocnName())
    );
  }
}
