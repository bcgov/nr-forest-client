package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.bcregistry.ClientDetailsDto;
import ca.bc.gov.app.dto.client.ClientListDto;
import ca.bc.gov.app.dto.client.ClientLookUpDto;
import ca.bc.gov.app.dto.legacy.ForestClientDetailsDto;
import ca.bc.gov.app.exception.NoClientDataFound;
import ca.bc.gov.app.service.client.ClientLegacyService;
import ca.bc.gov.app.service.client.ClientService;
import ca.bc.gov.app.util.JwtPrincipalUtil;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.WordUtils;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/clients", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientController {

  private final ClientService clientService;
  private final ClientLegacyService clientLegacyService;

  /**
   * Retrieves the details of a client based on the provided incorporation number.
   *
   * <p>This endpoint is used to fetch client details by their incorporation number. The request is
   * authenticated using a JWT, and additional information (such as user ID, business ID, and
   * provider) is extracted from the token to authorize the request.
   *
   * @param clientNumber the incorporation number of the client whose details are being requested
   * @param principal the JWT authentication token containing user and business information
   * @return a {@link Mono} emitting the {@link ClientDetailsDto} containing the client's details
   */
  @GetMapping("/{clientNumber}")
  public Mono<ClientDetailsDto> getClientDetailsByIncorporationNumber(
      @PathVariable String clientNumber,
      JwtAuthenticationToken principal
  ) {
    log.info("Requesting client details for client number {} from the client service. {}",
        clientNumber,
        JwtPrincipalUtil.getUserId(principal)
    );
    return clientService
        .getClientDetailsByIncorporationNumber(
            clientNumber,
            JwtPrincipalUtil.getUserId(principal),
            JwtPrincipalUtil.getBusinessId(principal),
            JwtPrincipalUtil.getProvider(principal)
        );
  }
  
  /**
   * Handles HTTP GET requests to retrieve client details based on the provided client number.
   *
   * <p>This method fetches the details of a client from the {@code ClientService} using the 
   * specified {@code clientNumber}. The caller's JWT authentication token is used to extract 
   * user-related information such as groups and user ID.</p>
   *
   * @param clientNumber the unique identifier of the client whose details are to be retrieved.
   * @param principal    the {@link JwtAuthenticationToken} containing the authenticated user's 
   *                     information, including their roles and groups.
   * @return a {@link Mono} emitting the {@link ForestClientDetailsDto} containing the requested 
   *         client details, or an error if the client cannot be found or accessed.
   */
  @GetMapping("/details/{clientNumber}")
  public Mono<ForestClientDetailsDto> getClientDetailsByClientNumber(
      @PathVariable String clientNumber,
      JwtAuthenticationToken principal
  ) {   
    log.info("Requesting client details for client number {} from the client service. {}",
        clientNumber,
        JwtPrincipalUtil.getUserId(principal)
    );
    return clientService.getClientDetailsByClientNumber(
            clientNumber,
            JwtPrincipalUtil.getGroups(principal));
  }
  
  /**
   * Performs a full-text search for clients based on the provided keyword, with pagination support.
   *
   * <p>This endpoint allows searching for clients by a keyword. The results are paginated, and the 
   * total count of matching records is included in the response headers.
   *
   * @param page the page number to retrieve (default is 0)
   * @param size the number of records per page (default is 10)
   * @param keyword the keyword to search for (default is an empty string, which returns all 
   *        records)
   * @param serverResponse the HTTP response to include the total count of records in the headers
   * @return a {@link Flux} emitting {@link ClientListDto} objects containing the search results
   */
  @GetMapping("/search")
  public Flux<ClientListDto> fullSearch(
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "10") int size,
      @RequestParam(required = false, defaultValue = "") String keyword,
      ServerHttpResponse serverResponse
  ) {
    log.info("Listing clients: page={}, size={}, keyword={}", page, size, keyword);

    return clientLegacyService
        .search(page, size, keyword)
        .doOnNext(pair -> {
          Long count = pair.getSecond();

          serverResponse
              .getHeaders()
              .putIfAbsent(
                  ApplicationConstant.X_TOTAL_COUNT,
                  List.of(count.toString())
              );
        })
        .map(Pair::getFirst)
        .doFinally(signalType ->
          serverResponse
              .getHeaders()
              .putIfAbsent(
                  ApplicationConstant.X_TOTAL_COUNT,
                  List.of("0")
              )
        );
  }
  
  /**
   * Retrieve a Flux of ClientLookUpDto objects by searching for clients with a specific name.
   *
   * @param name The name to search for.
   * @return A Flux of ClientLookUpDto objects that match the given name.
   */
  @GetMapping(value = "/name/{name}")
  public Flux<ClientLookUpDto> findByClientName(@PathVariable String name) {
    log.info("Requesting a list of clients with name {} from the client service.", name);
    return clientService
        .findByClientNameOrIncorporation(name)
        .map(client -> client.withName(WordUtils.capitalize(client.name())));
  }

  /**
   * Finds a client based on their registration number.
   *
   * <p>This endpoint retrieves client information by searching for a registration number. 
   * If no client is found, an error is returned.
   *
   * @param registrationNumber the registration number of the client to look up
   * @return a {@link Mono} emitting the {@link ClientLookUpDto} if found, or an error 
   *         if no data exists
   */
  @GetMapping(value = "/incorporation/{registrationNumber}")
  public Mono<ClientLookUpDto> findByRegistrationNumber(
      @PathVariable String registrationNumber) {
    log.info("Requesting a client with registration number {} from the client service.",
             registrationNumber);
    return clientService
        .findByClientNameOrIncorporation(registrationNumber)
        .next()
        .switchIfEmpty(Mono.error(new NoClientDataFound(registrationNumber)));
  }

  /**
   * Searches for an individual client by user ID and last name.
   *
   * <p>This endpoint fetches an individual client using their user ID and last name. 
   * The request is validated against existing records in the system.
   *
   * @param userId the unique identifier of the individual to search for
   * @param lastName the last name of the individual to search for
   * @return a {@link Mono} indicating completion, or an error if the individual is not found
   */
  @GetMapping(value = "/individual/{userId}")
  public Mono<Void> findByIndividual(
      @PathVariable String userId,
      @RequestParam String lastName
  ) {
    log.info("Receiving request to search individual with id {} and last name {}", 
             userId,
             lastName);
    return clientService.findByIndividual(userId, lastName);
  }

}
