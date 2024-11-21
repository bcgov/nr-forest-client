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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
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
            JwtPrincipalUtil.getUserId(principal),
            JwtPrincipalUtil.getBusinessId(principal),
            JwtPrincipalUtil.getProvider(principal));
  }
  
  @GetMapping("/search")
  public Flux<ClientListDto> fullSearch(
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "10") int size,
      @RequestParam(required = false, defaultValue = "") String keyword,
      ServerHttpResponse serverResponse) {
    
    log.info("Listing clients: page={}, size={}, keyword={}", page, size, keyword);
    
    return clientLegacyService
        .search(
            page,
            size,
            keyword
        )
        .doOnNext(pair -> {
          Long count = pair.getSecond();

          serverResponse
            .getHeaders()
            .putIfAbsent(
                ApplicationConstant.X_TOTAL_COUNT, 
                List.of(count.toString())
            );
          }
        )
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

  @GetMapping(value = "/individual/{userId}")
  public Mono<Void> findByIndividual(
      @PathVariable String userId,
      @RequestParam String lastName
  ) {
    log.info("Receiving request to search individual with id {} and last name {}", userId,
        lastName);
    return clientService.findByIndividual(userId, lastName);
  }

}
