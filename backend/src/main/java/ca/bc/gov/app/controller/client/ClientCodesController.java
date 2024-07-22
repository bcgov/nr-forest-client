package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.client.DistrictDto;
import ca.bc.gov.app.dto.client.IdentificationTypeDto;
import ca.bc.gov.app.service.client.ClientService;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDate;
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
@RequestMapping(value = "/api/codes", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientCodesController {

  private final ClientService clientService;

  @GetMapping("/client-types")
  public Flux<CodeNameDto> findActiveClientTypeCodes() {
    log.info("Requesting a list of active client type codes from the client service.");
    return clientService
        .findActiveClientTypeCodes(LocalDate.now());
  }

  @GetMapping("/client-types/{code}")
  public Mono<CodeNameDto> getClientTypeByCode(
      @PathVariable String code) {
    log.info("Requesting a client type by code {} from the client service.", code);
    return clientService.getClientTypeByCode(code);
  }

  @GetMapping("/contact-types")
  public Flux<CodeNameDto> listClientContactTypeCodes(
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size
  ) {
    log.info("Requesting a list of active client contact type codes from the client service.");
    return clientService
        .listClientContactTypeCodes(LocalDate.now(), page, size);
  }
  
  @GetMapping("/districts")
  public Flux<CodeNameDto> getActiveDistrictCodes(
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size) {
    log.info("Requesting a list of districts from the client service.");
    return clientService.getActiveDistrictCodes(page, size);
  }

  @GetMapping("/districts/{districtCode}")
  public Mono<DistrictDto> getDistrictByCode(@PathVariable String districtCode) {
    log.info("Requesting a district by code {} from the client service.", districtCode);
    return clientService.getDistrictByCode(districtCode);
  }
  
  @GetMapping("/countries")
  public Flux<CodeNameDto> countries(
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size) {
    log.info("Requesting a list of countries from the client service.");
    return clientService.listCountries(page, size);
  }

  @GetMapping("/countries/{countryCode}")
  public Mono<CodeNameDto> getCountryByCode(
      @PathVariable String countryCode) {
    log.info("Requesting a country by code {} from the client service.", countryCode);
    return clientService.getCountryByCode(countryCode);
  }

  @GetMapping("/countries/{countryCode}/provinces")
  public Flux<CodeNameDto> listProvinces(
      @PathVariable String countryCode,
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size) {
    log.info("Requesting a list of provinces by country code {} from the client service.",
        countryCode);
    return clientService
        .listProvinces(countryCode, page, size);
  }
  
  @GetMapping("/identification-types")
  public Flux<IdentificationTypeDto> identificationTypes() {
    log.info("Requesting a list of identification type codes.");
    return clientService.getAllActiveIdentificationTypes(LocalDate.now());
  }

  @GetMapping("{idCode}")
  public Mono<CodeNameDto> getIdentificationTypeByCode(
      @PathVariable String idCode) {
    log.info("Requesting an identification type by code {}.", idCode);
    return clientService.getIdentificationTypeByCode(idCode);
  }

}
