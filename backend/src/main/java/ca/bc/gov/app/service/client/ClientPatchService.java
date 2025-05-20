package ca.bc.gov.app.service.client;

import ca.bc.gov.app.service.client.validators.PatchValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.function.BinaryOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
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
  private final List<PatchValidator> validators;
  private final ObjectMapper mapper;

  /**
   * Sends a patch request to the legacy system to update a client.
   *
   * @param clientNumber The unique identifier of the client to update.
   * @param forestClient The JSON Patch document describing the modifications.
   * @param userName     The user that triggered the patch request
   * @return A {@link Mono} that completes when the patch is applied successfully.
   */
  public Mono<Void> patchClient(
      String clientNumber,
      JsonNode forestClient,
      String userName
  ) {
    log.info("{} requested to patch client {}", userName, clientNumber);

    return
        Flux
            .fromIterable(validators)
            .flatMap(validator ->
                Flux
                    .fromIterable(forestClient)
                    .flatMap(node ->
                        Mono
                            .just(node)
                            .filter(validator.shouldValidate())
                            .flatMap(validator.validate())
                            .defaultIfEmpty(node)
                        )
            )
            .reduce(mapper.createArrayNode(), mergeNodes())
            .flatMap(node ->
                legacyService.patchClient(clientNumber, node, userName)
            );
  }

  private BinaryOperator<JsonNode> mergeNodes() {
    return (node1, node2) -> {
      ArrayNode arrayNode = mapper.createArrayNode();
      if (node1 instanceof ArrayNode) {
        arrayNode = node1.deepCopy();
      } else {
        arrayNode.add(node1);
      }
      arrayNode.add(node2);
      return arrayNode;
    };
  }
}
