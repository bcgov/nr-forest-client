package ca.bc.gov.app.service.patch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service responsible for applying **JSON Patch** operations to a forest client.
 * <p>
 * This service processes a JSON Patch request by delegating the update to a list of
 * {@link ClientPatchOperation} implementations. Each registered operation is given a chance to
 * apply changes based on the provided patch data.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Observed
public class ClientPatchService {

  private final ObjectMapper mapper;
  private final List<ClientPatchOperation> partialServices;

  /**
   * Applies the JSON Patch updates to a forest client by invoking all available
   * {@link ClientPatchOperation} services.
   * <p>
   * Each service in {@code partialServices} attempts to apply the patch if relevant. The updates
   * are chained reactively, ensuring all applicable patches are processed before completion.
   * </p>
   *
   * @param clientNumber The unique identifier of the forest client being updated.
   * @param forestClient The JSON Patch document describing the modifications.
   * @param userId The username that requested the patch.
   * @return A {@link Mono} that completes when all patches have been applied.
   */
  public Mono<Void> patchClient(String clientNumber, JsonNode forestClient, String userId) {
    log.info("Patching client with client number {} if any changes are detected", clientNumber);

    return partialServices
        .stream()
        .map(service -> service.applyPatch(clientNumber, forestClient, mapper, userId))
        .reduce(Mono.empty(), Mono::then);
  }
}
