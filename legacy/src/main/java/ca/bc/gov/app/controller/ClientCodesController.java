package ca.bc.gov.app.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.bc.gov.app.dto.CodeNameDto;
import ca.bc.gov.app.service.ClientCodeService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(value = "/api/codes", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientCodesController {

  private final ClientCodeService clientCodeService;
  
  @GetMapping("/update-reasons/{clientTypeCode}/{actionCode}")
  public Flux<CodeNameDto> findActiveByClientTypeAndActionCode(
      @PathVariable String clientTypeCode,
      @PathVariable String actionCode) {
    log.info("Requesting a list of active client update reason codes from the client service.");
    return clientCodeService
        .findActiveByClientTypeAndActionCode(clientTypeCode, actionCode);
  }

  @GetMapping("/client-statuses")
  public Flux<CodeNameDto> findActiveClientStatusCodes() {
    log.info("Requesting a list of active client status codes from the client service.");
    return clientCodeService.findActiveClientStatusCodes();
  }
  
  @GetMapping("/registry-types")
  public Flux<CodeNameDto> findActiveRegistryTypeCodes() {
    log.info("Requesting a list of active client status codes.");
    return clientCodeService.findActiveRegistryTypeCodes();
  }
  
  @GetMapping("/registry-types/{clientTypeCode}")
  public Flux<CodeNameDto> findActiveRegistryTypeCodesByClientTypeCode(
      @PathVariable String clientTypeCode) {
    log.info("Requesting a list of active client status codes.");
    return clientCodeService.findActiveRegistryTypeCodesByClientTypeCode(clientTypeCode);
  }
  
  @GetMapping("/client-types")
  public Flux<CodeNameDto> findActiveClientTypeCodes() {
    log.info("Requesting a list of active client type codes.");
    return clientCodeService.findActiveClientTypeCodes();
  }
  
  @GetMapping("/client-id-types")
  public Flux<CodeNameDto> findActiveClientIdTypeCodes() {
    log.info("Requesting a list of active client ID type codes.");
    return clientCodeService.findActiveClientIdTypeCodes();
  }
  
}
