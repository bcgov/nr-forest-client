package ca.bc.gov.app.controller.client;

import java.time.LocalDate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.client.IdentificationTypeDto;
import ca.bc.gov.app.service.client.ClientService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/identification-types", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Observed
public class IdentificationTypesController {
  
  private final ClientService clientService;

  @GetMapping
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
