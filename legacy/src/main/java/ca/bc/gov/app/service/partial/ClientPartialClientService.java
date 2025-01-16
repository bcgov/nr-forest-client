package ca.bc.gov.app.service.partial;

import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.repository.ForestClientRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@Observed
@RequiredArgsConstructor
public class ClientPartialClientService implements ClientPartialService<ForestClientEntity> {

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

    if (checkOperation(patch, getPrefix(), mapper)) {
      JsonNode filteredNode = filterPatchOperations(patch, mapper);

      log.info("Base client change {}", filteredNode);

      return
          clientRepository
              .findByClientNumber(clientNumber)
              .map(entity -> patchClient(
                      filteredNode,
                      entity,
                      ForestClientEntity.class,
                      mapper
                  )
              )
              .flatMap(clientRepository::save)
              .then();
    }

    return Mono.empty();
  }


}
