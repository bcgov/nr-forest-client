package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.service.client.ClientAddressService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping(value = "/api/addresses", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Observed
public class ClientAddressController {

  private final ClientAddressService clientAddressService;

  @GetMapping
  public Flux<CodeNameDto> findPossibleAddresses(
      @RequestParam(value = "country", required = false, defaultValue = "CA")
      String country,
      @RequestParam(value = "maxSuggestions", required = false, defaultValue = "7")
      Integer maxSuggestions,
      @RequestParam(value = "searchTerm", required = true)
      String searchTerm) {
    log.info("Requesting possible addresses for country: {}, maxSuggestions: {}, searchTerm: {}",
        country, maxSuggestions, searchTerm);
    return clientAddressService
        .findPossibleAddresses(country, maxSuggestions, searchTerm);
  }

  @GetMapping("/{addressId}")
  public Mono<ClientAddressDto> getAddress(
      @PathVariable String addressId) {
    log.info("Requesting address for addressId: {}", addressId);
    return clientAddressService
        .getAddress(addressId);
  }
}
