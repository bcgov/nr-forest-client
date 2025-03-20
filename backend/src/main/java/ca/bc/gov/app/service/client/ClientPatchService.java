package ca.bc.gov.app.service.client;

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
   * @param userName The user that triggered the patch request
   * @return A {@link Mono} that completes when the patch is applied successfully.
   */
  public Mono<Void> patchClient(
      String clientNumber,
      Object forestClient,
      String userName
  ) {
    log.info("{} requested to patch client {}", userName, clientNumber);
    return legacyService.patchClient(clientNumber, forestClient, userName);
  }
}
