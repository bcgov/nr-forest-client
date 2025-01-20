package ca.bc.gov.app.service.partial;

import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.repository.ForestClientRepository;
import ca.bc.gov.app.utils.PatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@Observed
@RequiredArgsConstructor
@Order(1)
public class ClientPatchOperationClientService implements ClientPatchOperationService {

  private final ForestClientRepository clientRepository;

  public String getPrefix() {
    return "client";
  }

  public List<String> getRestrictedPaths() {
    return List.of(
        "/clientNumber",
        "/clientName",
        "/legalFirstName",
        "/legalMiddleName",
        "/clientStatusCode",
        "/clientTypeCode",
        "/clientIdTypeCode",
        "/clientIdentification",
        "/registryCompanyTypeCode",
        "/corpRegnNmbr",
        "/clientAcronym",
        "/wcbFirmNumber",
        "/clientComment",
        "/birthdate"
    );
  }

  /**
   * Applies a JSON Patch to a ForestClientEntity identified by the given client number.
   * This is super straightforward, just a few lines of code. First it checks if the patch
   * is applicable to the ForestClientEntity, then it filters the patch operations to only
   * include the ones  listed on restricted paths.
   * Finally, it applies the patch to the entity and saves it.
   *
   * @param clientNumber the client number identifying the ForestClientEntity
   * @param patch the JSON Patch to apply
   * @param mapper the ObjectMapper used for JSON processing
   * @return a Mono that completes when the patch has been applied
   */
  public Mono<Void> applyPatch(
      String clientNumber,
      JsonPatch patch,
      ObjectMapper mapper
  ) {

    if (PatchUtils.checkOperation(patch, getPrefix(), mapper)) {
      JsonNode filteredNode = PatchUtils.filterPatchOperations(
          patch,
          getPrefix(),
          getRestrictedPaths(),
          mapper
      );

      return
          clientRepository
              .findByClientNumber(clientNumber)
              .flatMap(entity ->

                  Mono
                      .just(
                      PatchUtils.patchClient(
                          filteredNode,
                          entity,
                          ForestClientEntity.class,
                          mapper
                      )
                  )
                  .filter(client -> !entity.equals(client))
                  .doOnNext(client -> log.info("Applying Forest Client changes {}", client))
              )
              .flatMap(clientRepository::save)
              .then();
    }

    return Mono.empty();
  }

}
