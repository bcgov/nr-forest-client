package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.service.client.ClientService;
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
@RequestMapping(value = "/api/countries", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Observed
public class CountryController {

  private final ClientService clientService;

  @GetMapping
  public Flux<CodeNameDto> countries(
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size) {
    log.info("Requesting a list of countries from the client service.");
    return clientService.listCountries(page, size);
  }

  @GetMapping("{countryCode}")
  public Mono<CodeNameDto> getCountryByCode(
      @PathVariable String countryCode) {
    log.info("Requesting a country by code {} from the client service.", countryCode);
    return clientService.getCountryByCode(countryCode);
  }

  @GetMapping("/{countryCode}/provinces")
  public Flux<CodeNameDto> listProvinces(
      @PathVariable String countryCode,
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size) {
    log.info("Requesting a list of provinces for country code {} from the client service.",
        countryCode);
    return clientService
        .listProvinces(countryCode, page, size);
  }

}
