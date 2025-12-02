package ca.bc.gov.app.service.patch;

import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.repository.ForestClientRepository;
import ca.bc.gov.app.util.PatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
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
public class PatchOperationClientService implements ClientPatchOperation {

  private final ForestClientRepository clientRepository;

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
    return List.of(
        "/wcbFirmNumber",
        "/clientComment",
        "/clientAcronym",
        "/birthdate",
        "/corpRegnNmbr",
        "/registryCompanyTypeCode",
        "/clientTypeCode"
    );
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
   * @param userId The username that requested the patch.
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

      if (filteredNode.isEmpty())
        return Mono.empty();

      return
          clientRepository
              .findByClientNumber(clientNumber)
              .flatMap(entity ->
                  patchForestClientEntity(mapper, userId, entity, filteredNode)
                      .doOnNext(client -> log.info("Applying Forest Client changes {}", client))
              )
              .flatMap(clientRepository::save)
              .then();
    }

    return Mono.empty();
  }
}
