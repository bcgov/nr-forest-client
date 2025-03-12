package ca.bc.gov.app.controller;

import ca.bc.gov.app.service.patch.ClientPatchService;
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
@RequestMapping(value = "/api/clients/partial", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Observed
public class ClientPatchController {

  private final ClientPatchService clientPatchService;

  @PatchMapping(value = "/{clientNumber}", consumes = "application/json-patch+json")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Mono<Void> patchForestClient(
      @PathVariable String clientNumber,
      @RequestBody Object forestClient
  ) {
    log.info("Received a partial update request for client {}", clientNumber);
    return clientPatchService.patchClient(clientNumber, forestClient);
  }

}
