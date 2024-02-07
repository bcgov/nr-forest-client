package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.dto.client.CodeNameDto;
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

  @GetMapping("/clientTypes")
  public Flux<CodeNameDto> findActiveClientTypeCodes() {
    log.info("Requesting a list of active client type codes from the client service.");
    return clientService
        .findActiveClientTypeCodes(LocalDate.now());
  }

  @GetMapping("/clientTypes/{code}")
  public Mono<CodeNameDto> getClientTypeByCode(
      @PathVariable String code) {
    log.info("Requesting a client type by code {} from the client service.", code);
    return clientService.getClientTypeByCode(code);
  }

  @GetMapping("/contactTypes")
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

}
