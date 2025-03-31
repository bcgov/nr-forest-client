package ca.bc.gov.app.service.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * The Client patch service. This is where the patch request is processed.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientPatchService {

  private final ClientLegacyService legacyService;

  /**
   * Sends a patch request to the legacy system to update a client.
   *
   * @param clientNumber The unique identifier of the client to update.
   * @param forestClient The JSON Patch document describing the modifications.
   * @param userName The username that requested the patch.
   * @return A {@link Mono} that completes when the patch is applied successfully.
   */
  public Mono<Void> patchClient(String clientNumber, JsonNode forestClient, String userName) {
    log.info("Sending request to the legacy system to patch client {}", clientNumber);
    return legacyService.patchClient(clientNumber, forestClient, userName);
  }
}
