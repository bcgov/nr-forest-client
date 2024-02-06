package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.bcregistry.ClientDetailsDto;
import ca.bc.gov.app.dto.client.ClientLookUpDto;
import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.client.EmailRequestDto;
import ca.bc.gov.app.exception.NoClientDataFound;
import ca.bc.gov.app.service.client.ClientService;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    log.info("Requesting client details for client number {} from the client service.", clientNumber);
    return clientService.getClientDetails(clientNumber,userId,businessId);
  }
  
  @GetMapping("/activeDistrictCodes")
  public Flux<CodeNameDto> listDistricts(
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size) {
    log.info("Requesting a list of districts from the client service.");
    return clientService.listDistricts(page, size);
  }
  
  @GetMapping("/getDistrictByCode/{districtCode}")
  public Mono<CodeNameDto> getDistrictByCode(@PathVariable String districtCode) {
    log.info("Requesting a district by code {} from the client service.", districtCode);
    return clientService.getDistrictByCode(districtCode);
  }

  @GetMapping("/activeCountryCodes")
  public Flux<CodeNameDto> listCountries(
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size) {
    log.info("Requesting a list of countries from the client service.");
    return clientService.listCountries(page, size);
  }

  @GetMapping("/getCountryByCode/{countryCode}")
  public Mono<CodeNameDto> getCountryByCode(
      @PathVariable String countryCode) {
    log.info("Requesting a country by code {} from the client service.", countryCode);
    return clientService.getCountryByCode(countryCode);
  }

  @GetMapping("/activeCountryCodes/{countryCode}")
  public Flux<CodeNameDto> listProvinces(
      @PathVariable String countryCode,
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size) {
    log.info("Requesting a list of provinces for country code {} from the client service.", countryCode);
    return clientService
        .listProvinces(countryCode, page, size);
  }

  @GetMapping("/getClientTypeByCode/{code}")
  public Mono<CodeNameDto> getClientTypeByCode(
      @PathVariable String code) {
    log.info("Requesting a client type by code {} from the client service.", code);
    return clientService.getClientTypeByCode(code);
  }
  
  @GetMapping("/activeClientTypeCodes")
  public Flux<CodeNameDto> findActiveClientTypeCodes() {
    log.info("Requesting a list of active client type codes from the client service.");
    return clientService
        .findActiveClientTypeCodes(LocalDate.now());
  }

  @GetMapping("/activeContactTypeCodes")
  public Flux<CodeNameDto> listClientContactTypeCodes(
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size
  ) {
    log.info("Requesting a list of active client contact type codes from the client service.");
    return clientService
        .listClientContactTypeCodes(LocalDate.now(),page, size);
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

  @GetMapping(value = "/incorporation/{incorporationId}")
  public Mono<ClientLookUpDto> findByIncorporationNumber(
      @PathVariable String incorporationId) {
    log.info("Requesting a client with incorporation number {} from the client service.", incorporationId);
    return clientService
        .findByClientNameOrIncorporation(incorporationId)
        .next()
        .switchIfEmpty(Mono.error(new NoClientDataFound(incorporationId)));
  }

  @GetMapping(value = "/individual/{userId}")
  public Mono<Void> findByIndividual(
      @PathVariable String userId,
      @RequestParam String lastName
  ) {
    log.info("Receiving request to search individual with id {} and last name {}", userId, lastName);
    return clientService.findByIndividual(userId,lastName);
  }

  @PostMapping("/mail")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Mono<Void> sendEmail(
      @RequestBody EmailRequestDto emailRequestDto,
      @RequestHeader(ApplicationConstant.USERID_HEADER) String userId,
      @RequestHeader(name = ApplicationConstant.BUSINESSID_HEADER, defaultValue = StringUtils.EMPTY) String businessId
      ) {
    log.info("Sending email to {} from the client service.", emailRequestDto.email());
    return clientService.triggerEmailDuplicatedClient(emailRequestDto, userId, businessId);
  }

}
