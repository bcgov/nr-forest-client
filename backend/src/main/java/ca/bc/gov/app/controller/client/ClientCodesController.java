package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.client.DistrictDto;
import ca.bc.gov.app.dto.client.IdentificationTypeDto;
import ca.bc.gov.app.service.client.ClientCodeService;
import ca.bc.gov.app.service.client.ClientCountryProvinceService;
import ca.bc.gov.app.service.client.ClientDistrictService;
import ca.bc.gov.app.service.client.ClientService;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
  private final ClientDistrictService clientDistrictService;
  private final ClientCountryProvinceService clientCountryProvinceService;
  private final ClientCodeService clientCodeService;

  @GetMapping("/client-types")
  public Flux<CodeNameDto> findActiveClientTypeCodes() {
    log.info("Requesting a list of active client type codes from the client service.");
    return clientCodeService
        .findActiveClientTypeCodes(LocalDate.now());
  }

  @GetMapping("/client-types/{code}")
  public Mono<CodeNameDto> getClientTypeByCode(
      @PathVariable String code) {
    log.info("Requesting a client type by code {} from the client service.", code);
    return clientCodeService.getClientTypeByCode(code);
  }

  @GetMapping("/contact-types")
  public Flux<CodeNameDto> listClientContactTypeCodes(
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size
  ) {
    log.info("Requesting a list of active client contact type codes from the client service.");
    return clientCodeService
        .listClientContactTypeCodes(LocalDate.now(), page, size);
  }
  
  @GetMapping("/districts")
  public Flux<CodeNameDto> getActiveDistrictCodes(
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size) {
    log.info("Requesting a list of districts from the client service.");
    return clientDistrictService.getActiveDistrictCodes(page, size, LocalDate.now());
  }

  @GetMapping("/districts/{districtCode}")
  public Mono<DistrictDto> getDistrictByCode(@PathVariable String districtCode) {
    log.info("Requesting a district by code {} from the client service.", districtCode);
    return clientDistrictService.getDistrictByCode(districtCode);
  }
  
  @GetMapping("/countries")
  public Flux<CodeNameDto> countries(
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size) {
    log.info("Requesting a list of countries from the client service.");
    return clientCountryProvinceService.listCountries(page, size, LocalDate.now());
  }

  @GetMapping("/countries/{countryCode}")
  public Mono<CodeNameDto> getCountryByCode(
      @PathVariable String countryCode) {
    log.info("Requesting a country by code {} from the client service.", countryCode);
    return clientCountryProvinceService.getCountryByCode(countryCode);
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
    return clientCountryProvinceService
        .listProvinces(countryCode, page, size);
  }
  
  @GetMapping("/countries/{countryCode}/{provinceCode}")
  public Mono<CodeNameDto> getProvinceByCountryAndProvinceCode(
      @PathVariable String countryCode,
      @PathVariable String provinceCode) {
    log.info("Requesting a province by country and province code {} {} from the client service.",
             countryCode, 
             provinceCode);
    return clientCountryProvinceService.getProvinceByCountryAndProvinceCode(countryCode, provinceCode);
  }
  
  @GetMapping("/identification-types")
  public Flux<IdentificationTypeDto> identificationTypes() {
    log.info("Requesting a list of identification type codes.");
    return clientCodeService.getAllActiveIdentificationTypes(LocalDate.now());
  }

  //TODO: This endpoint needs to be updated to properly reflect what code is returning
  @GetMapping("{idCode}")
  public Mono<CodeNameDto> getIdentificationTypeByCode(
      @PathVariable String idCode) {
    log.info("Requesting an identification type by code {}.", idCode);
    return clientCodeService.getIdentificationTypeByCode(idCode);
  }

}
