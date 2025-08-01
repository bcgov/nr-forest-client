package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.client.DistrictDto;
import ca.bc.gov.app.dto.client.IdentificationTypeDto;
import ca.bc.gov.app.service.client.ClientCodeService;
import ca.bc.gov.app.service.client.ClientCountryProvinceService;
import ca.bc.gov.app.service.client.ClientDistrictService;
import ca.bc.gov.app.service.client.ClientLegacyService;
import ca.bc.gov.app.util.JwtPrincipalUtil;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST controller for retrieving various client-related code data, such as districts, provinces,
 * and other code-based information. All endpoints under this controller produce JSON responses and
 * are prefixed with {@code /api/codes}.
 */
@RestController
@RequestMapping(value = "/api/codes", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientCodesController {

  private final ClientDistrictService clientDistrictService;
  private final ClientCountryProvinceService clientCountryProvinceService;
  private final ClientCodeService clientCodeService;
  private final ClientLegacyService legacyService;
  
  /**
   * Retrieves a list of active client type codes.
   *
   * @return a {@link Flux} containing {@link CodeNameDto} for active client types
   */
  @GetMapping("/client-types")
  public Flux<CodeNameDto> findActiveClientTypeCodes() {
    log.info("Requesting a list of active client type codes from the client service.");
    return clientCodeService
        .findActiveClientTypeCodes(LocalDate.now());
  }

  /**
   * Retrieves a specific client type by its code.
   *
   * @param code the client type code
   * @return a {@link Mono} containing the {@link CodeNameDto} of the specified client type
   */
  @GetMapping("/client-types/{code}")
  public Mono<CodeNameDto> getClientTypeByCode(
      @PathVariable String code) {
    log.info("Requesting a client type by code {} from the client service.", code);
    return clientCodeService.getClientTypeByCode(code);
  }

  /**
   * Retrieves a paginated list of active client contact type codes.
   *
   * @param page the page number (default is 0)
   * @param size the page size (default is 10)
   * @return a {@link Flux} containing {@link CodeNameDto} for contact types
   */
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
  
  /**
   * Retrieves a paginated list of active district codes.
   *
   * @param page the page number (default is 0)
   * @param size the page size (default is 10)
   * @return a {@link Flux} containing {@link CodeNameDto} for districts
   */
  @GetMapping("/districts")
  public Flux<CodeNameDto> getActiveDistrictCodes(
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size) {
    log.info("Requesting a list of districts from the client service.");
    return clientDistrictService.getActiveDistrictCodes(page, size, LocalDate.now());
  }

  /**
   * Retrieves a district by its code.
   *
   * @param districtCode the code of the district
   * @return a {@link Mono} containing the {@link DistrictDto} for the given code
   */
  @GetMapping("/districts/{districtCode}")
  public Mono<DistrictDto> getDistrictByCode(@PathVariable String districtCode) {
    log.info("Requesting a district by code {} from the client service.", districtCode);
    return clientDistrictService.getDistrictByCode(districtCode);
  }
  
  /**
   * Retrieves a paginated list of countries.
   *
   * @param page the page number (default is 0)
   * @param size the page size (default is 10)
   * @return a {@link Flux} containing {@link CodeNameDto} for countries
   */
  @GetMapping("/countries")
  public Flux<CodeNameDto> countries(
      @RequestParam(value = "page", required = false, defaultValue = "0")
      Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10")
      Integer size) {
    log.info("Requesting a list of countries from the client service.");
    return clientCountryProvinceService.listCountries(page, size, LocalDate.now());
  }

  /**
   * Retrieves a country by its code.
   *
   * @param countryCode the code of the country
   * @return a {@link Mono} containing the {@link CodeNameDto} for the given country code
   */
  @GetMapping("/countries/{countryCode}")
  public Mono<CodeNameDto> getCountryByCode(
      @PathVariable String countryCode) {
    log.info("Requesting a country by code {} from the client service.", countryCode);
    return clientCountryProvinceService.getCountryByCode(countryCode);
  }

  /**
   * Retrieves a paginated list of provinces for a given country.
   *
   * @param countryCode the code of the country
   * @param page the page number (default is 0)
   * @param size the page size (default is 10)
   * @return a {@link Flux} containing {@link CodeNameDto} for provinces
   */
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
  
  /**
   * Retrieves a province by its country and province codes.
   *
   * @param countryCode the code of the country
   * @param provinceCode the code of the province
   * @return a {@link Mono} containing the {@link CodeNameDto} for the specified province
   */
  @GetMapping("/countries/{countryCode}/{provinceCode}")
  public Mono<CodeNameDto> getProvinceByCountryAndProvinceCode(
      @PathVariable String countryCode,
      @PathVariable String provinceCode) {
    log.info("Requesting a province by country and province code {} {} from the client service.",
             countryCode, 
             provinceCode);
    return clientCountryProvinceService.getProvinceByCountryAndProvinceCode(
        countryCode, 
        provinceCode);
  }
  
  /**
   * Retrieves a list of all active identification type codes.
   *
   * @return a {@link Flux} containing {@link IdentificationTypeDto} for identification types
   */
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

  /**
   * Handles HTTP GET requests to retrieve a list of active update reason codes for a specific 
   * client type and action code. 
   * This endpoint interacts with the client service to fetch the relevant data.
   *
   * @param clientTypeCode the code representing the type of client (e.g., individual, corporation)
   * @param actionCode the code representing the action being performed (e.g., name change, address
   *        change)
   * @return a {@link Flux} emitting {@link CodeNameDto} objects representing the active update
   *         reasons for the specified client type and action code
   */
  @GetMapping("/update-reasons/{clientTypeCode}/{actionCode}")
  public Flux<CodeNameDto> findActiveByClientTypeAndActionCode(
      @PathVariable String clientTypeCode,
      @PathVariable String actionCode) {
    return legacyService.findActiveUpdateReasonsByClientTypeAndActionCode(
        clientTypeCode,
        actionCode);
  }
  
  /**
   * Retrieves a list of all active client status codes from the legacy client service.
   *
   * @return a {@link Flux} stream of {@link CodeNameDto} representing active client statuses
   */
  @GetMapping("/client-statuses")
  public Flux<CodeNameDto> findActiveClientStatusCodes() {
    log.info("Requesting a list of active client status codes from the client service.");
    return legacyService.findActiveClientStatusCodes();
  }
  
  /**
   * Retrieves a list of active client status codes based on client type and user role groups.
   *
   * @param clientTypeCode the client type code used to filter the statuses
   * @param principal      the authenticated user used to determine role-based access
   * @return a {@link Flux} stream of {@link CodeNameDto} filtered by client type and roles
   */
  @GetMapping("/client-statuses/{clientTypeCode}")
  public Flux<CodeNameDto> findActiveClientStatusCodes(
      @PathVariable String clientTypeCode,
      JwtAuthenticationToken principal) {
    log.info("Requesting a list of active client status codes from the client service.");
    return legacyService
        .findActiveClientStatusCodesByClientTypeAndRole(
            clientTypeCode,
            JwtPrincipalUtil.getGroups(principal)
    );
  }
  
  /**
   * Retrieves a list of all active registry type codes from the legacy client service.
   *
   * @return a {@link Flux} stream of {@link CodeNameDto} representing active registry types
   */
  @GetMapping("/registry-types")
  public Flux<CodeNameDto> findActiveRegistryTypeCodes() {
    log.info("Requesting a list of active registry type codes from the client service.");
    return legacyService.findActiveRegistryTypeCodes();
  }
  
  /**
   * Retrieves a list of active registry type codes for the specified client type.
   *
   * @param clientTypeCode the client type code used to filter the registry types
   * @return a {@link Flux} stream of {@link CodeNameDto} filtered by client type
   */
  @GetMapping("/registry-types/{clientTypeCode}")
  public Flux<CodeNameDto> findActiveRegistryTypeCodesByClientTypeCode(
      @PathVariable String clientTypeCode) {
    log.info("Requesting a list of active registry type codes by client type from legacy.");
    return legacyService.findActiveRegistryTypeCodesByClientTypeCode(clientTypeCode);
  }
  
  /**
   * Retrieves a list of all active client type codes from the legacy system as of today.
   *
   * @return a {@link Flux} stream of {@link CodeNameDto} representing active client types
   */
  @GetMapping("/client-types/legacy")
  public Flux<CodeNameDto> findActiveClientTypeCodesInLegacy() {
    log.info("Requesting a list of active client type codes from legacy.");
    return legacyService
        .findActiveClientTypeCodes();
  }
  
  /**
   * Retrieves a list of all active identification type codes from the legacy system.
   *
   * @return a {@link Flux} stream of {@link CodeNameDto} representing active ID types
   */
  @GetMapping("/identification-types/legacy")
  public Flux<CodeNameDto> findActiveIdentificationTypeCodes() {
    log.info("Requesting a list of active ID type codes from legacy.");
    return legacyService.findActiveIdentificationTypeCodes();
  }
  
  @GetMapping("/relationship-types/{clientTypeCode}")
  public Flux<CodeNameDto> findActiveRelationshipCodesByClientTypeCode(
      @PathVariable String clientTypeCode) {
    log.info("Requesting a list of active relationship codes by client type from legacy.");
    return legacyService.findActiveRelationshipCodesByClientTypeCode(clientTypeCode);
  }
  
}
