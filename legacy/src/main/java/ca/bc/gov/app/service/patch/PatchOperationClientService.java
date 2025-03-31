package ca.bc.gov.app.service.patch;

import ca.bc.gov.app.ApplicationConstants;
import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.util.PatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service responsible for applying JSON Patch operations to a {@link ForestClientEntity}.
 * <p>
 * This service handles patch operations targeting client-related data. It ensures that restricted
 * fields (such as {@code /wcbFirmNumber} and {@code /clientComment}) are the only ones being
 * modified. The patch is applied only if it matches the expected prefix.
 * </p>
 */
@Service
@Slf4j
@Observed
@RequiredArgsConstructor
@Order(1)
public class PatchOperationClientService implements ClientPatchUpdateOperation<ForestClientEntity> {

  private final R2dbcEntityOperations entityTemplate;

  /**
   * Returns the prefix associated with this patch operation.
   * <p>
   * Patches with this prefix are processed by this service. In this case, only patches related to
   * "client" fields are handled.
   * </p>
   *
   * @return The string prefix "client".
   */
  @Override
  public String getPrefix() {
    return "client";
  }

  /**
   * Returns a list of paths that are restricted from modification.
   * <p>
   * These fields are protected and cannot be updated through JSON Patch operations. Attempts to
   * modify them will be filtered out before applying the patch.
   * </p>
   *
   * @return A list of restricted JSON Patch paths.
   */
  @Override
  public List<String> getRestrictedPaths() {
    return List.of("/wcbFirmNumber", "/clientComment");
  }

  @Override
  public Map<String, String> getFieldToDataField() {
    return Map.of(
        "/wcbFirmNumber", "wcb_firm_number",
        "/clientComment", "client_comment"
    );
  }

  @Override
  public Function<ForestClientEntity, Map<String, Object>> getExtraFields(String userId) {
    return entity -> Map.of(
        "update_timestamp", LocalDateTime.now(),
        "update_userid", userId,
        "update_org_unit", 70L,
        "revision_count", entity.getRevision() + 1
    );
  }

  @Override
  public Mono<ForestClientEntity> findEntity(String clientNumber, String entityId) {
    return entityTemplate
        .selectOne(
            getEntityIdentification(clientNumber, entityId),
            getEntityClass()
        );
  }

  @Override
  public Query getEntityIdentification(String clientNumber, String entityId) {
    return Query
        .query(
            Criteria
                .where(ApplicationConstants.CLIENT_NUMBER).is(clientNumber)
        );
  }

  @Override
  public Class<ForestClientEntity> getEntityClass() {
    return ForestClientEntity.class;
  }

  /**
   * Applies a JSON Patch to a ForestClientEntity identified by the given client number. This is
   * super straightforward, just a few lines of code. First it checks if the patch is applicable to
   * the ForestClientEntity, then it filters the patch operations to only include the ones  listed
   * on restricted paths. Finally, it applies the patch to the entity and saves it.
   *
   * @param clientNumber the client number identifying the ForestClientEntity
   * @param patch        the JSON Patch to apply
   * @param mapper       the ObjectMapper used for JSON processing
   * @param userId       The username that requested the patch.
   * @return a Mono that completes when the patch has been applied
   */
  @Override
  public Mono<Void> applyPatch(
      String clientNumber,
      JsonNode patch,
      ObjectMapper mapper,
      String userId
  ) {

    if (PatchUtils.checkOperation(patch, getPrefix(), mapper)) {
      JsonNode filteredNode = PatchUtils.filterPatchOperations(
          patch,
          getPrefix(),
          getRestrictedPaths(),
          mapper
      );

      return applyReplacePatch(clientNumber, filteredNode, mapper, userId, entityTemplate);
    }
    return Mono.empty();
  }

}
