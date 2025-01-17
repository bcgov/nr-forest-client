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
