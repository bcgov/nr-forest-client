package ca.bc.gov.app.service.patch;

import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.util.PatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 * Represents a patch operation that can be applied to a specific section of a forest client.
 * <p>
 * Implementations of this interface define how JSON Patch updates should be handled
 * for different parts of a client's data. Each operation is responsible for a specific
 * subset of fields, identified by a unique prefix.
 * </p>
 */
public interface ClientPatchOperation {

  /**
   * Returns the prefix associated with this patch operation.
   * <p>
   * The prefix is used to determine if this operation should handle a given patch.
   * For example, a prefix of {@code /contact} means this service applies patches
   * only to client contact details.
   * </p>
   *
   * @return A string representing the patch operation's prefix.
   */
  String getPrefix();

  /**
   * Returns a list of paths that are restricted from modification.
   * <p>
   * Certain fields may be protected from updates for security or integrity reasons.
   * This method provides a list of JSON Patch paths that should be ignored or rejected.
   * </p>
   *
   * @return A list of JSON Patch paths that are restricted.
   */
  List<String> getRestrictedPaths();

  /**
   * Applies the JSON Patch operation to the specified client data.
   * <p>
   * The method receives a patch request and processes it based on the given
   * {@code clientNumber}. The operation should leverage the provided {@link ObjectMapper}
   * to parse and apply the patch.
   * </p>
   *
   * @param clientNumber The unique identifier of the client to be patched.
   * @param patch The JSON Patch document describing the changes.
   * @param mapper The {@link ObjectMapper} used to deserialize and apply the patch.
   * @param userId The username of the user who triggered the request.
   * @return A {@link Mono} that completes when the patch has been applied.
   */
  Mono<Void> applyPatch(String clientNumber, JsonNode patch, ObjectMapper mapper, String userId);

  default Mono<ForestClientEntity> patchForestClientEntity(
      ObjectMapper mapper,
      String userId,
      ForestClientEntity entity,
      JsonNode filteredNode
  ) {
    return Mono
        .just(
            PatchUtils.patchClient(
                filteredNode,
                entity,
                ForestClientEntity.class,
                mapper
            )
        )
        .filter(client -> !entity.equals(client))
        .map(client -> client
            .withUpdatedBy(userId)
            .withUpdatedAt(LocalDateTime.now())
            .withRevision(client.getRevision() + 1)
        )
        //Can only happen if there's a change
        .map(client ->
            client
                .withUpdatedAt(LocalDateTime.now())
                .withUpdatedBy(userId) // Is still missing the user org unit
                .withRevision(client.getRevision() + 1)
        );
  }

}

