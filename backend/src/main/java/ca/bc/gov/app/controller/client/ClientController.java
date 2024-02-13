package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.bcregistry.ClientDetailsDto;
import ca.bc.gov.app.dto.client.ClientLookUpDto;
import ca.bc.gov.app.exception.NoClientDataFound;
import ca.bc.gov.app.service.client.ClientService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
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

  @GetMapping("/{clientNumber}")
  public Mono<ClientDetailsDto> getClientDetails(
      @PathVariable String clientNumber,
      @RequestHeader(ApplicationConstant.USERID_HEADER) String userId,
      @RequestHeader(name = ApplicationConstant.BUSINESSID_HEADER, defaultValue = StringUtils.EMPTY) String businessId
  ) {
    log.info("Requesting client details for client number {} from the client service.",
        clientNumber);
    return clientService.getClientDetails(clientNumber, userId, businessId);
  }

  /**
   * Retrieve a Flux of ClientLookUpDto objects by searching for clients with a specific name.
   *
   * @param name The name to search for.
   * @return A Flux of ClientLookUpDto objects that match the given name.
   */
  @GetMapping(value = "/name/{name}")
  public Flux<ClientLookUpDto> findByClientName(
      @PathVariable String name
  ) {
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
