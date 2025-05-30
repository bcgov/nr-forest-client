package ca.bc.gov.app.controller;

import static ca.bc.gov.app.ApplicationConstants.MDC_USERID;

import ca.bc.gov.app.service.patch.ClientPatchService;
import com.fasterxml.jackson.databind.JsonNode;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * The Client patch controller. This is where the patch request arrives to be handled.
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/clients/partial", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Observed
public class ClientPatchController {

  private final ClientPatchService clientPatchService;

  /**
   * Handles a **JSON Patch** request to partially update a forest client.
   * <p>
   * This endpoint follows the JSON Patch specification as defined in
   * <a href="https://datatracker.ietf.org/doc/html/rfc6902">RFC 6902</a>.
   * Clients must send a request body formatted as a JSON Patch document
   * (`application/json-patch+json`).
   * </p>
   *
   * @param clientNumber The unique identifier of the forest client to update.
   * @param forestClient The JSON Patch document describing the modifications.
   * @return A {@link Mono} that completes when the patch is applied successfully.
   */
  @PatchMapping(value = "/{clientNumber}", consumes = "application/json-patch+json")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public Mono<Void> patchForestClient(
      @PathVariable String clientNumber,
      @RequestBody JsonNode forestClient,
      @RequestHeader(MDC_USERID) String userId
  ) {
    log.info("Received a partial update request for client {} from {}", clientNumber, userId);
    return clientPatchService.patchClient(clientNumber, forestClient, userId);
  }

}
