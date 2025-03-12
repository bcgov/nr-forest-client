package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.service.client.ClientPatchService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping(value = "/api/clients/details", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Observed
public class ClientPatchController {

  private final ClientPatchService service;

  @PatchMapping(value = "/{clientNumber}", consumes = "application/json-patch+json")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Mono<Void> patchForestClient(
      @PathVariable String clientNumber,
      @RequestBody Object forestClient //Setting as object because it's irrelevant for this service
  ) {
    log.info("Received a partial update request for client {}", clientNumber);
    return service.patchClient(clientNumber, forestClient);
  }

}
