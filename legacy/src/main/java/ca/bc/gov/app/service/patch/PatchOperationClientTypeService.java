package ca.bc.gov.app.service.patch;

import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.repository.ForestClientRepository;
import ca.bc.gov.app.util.PatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDateTime;
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
@Order(0)
public class PatchOperationClientTypeService implements ClientPatchOperation {

  private final ForestClientRepository clientRepository;

  @Override
  public String getPrefix() {
    return "client";
  }

  @Override
  public List<String> getRestrictedPaths() {
    return List.of("/clientTypeCode");
  }

  @Override
  public Mono<Void> applyPatch(String clientNumber, JsonNode patch, ObjectMapper mapper,
      String userId) {
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
                  Mono
                      .just(
                          PatchUtils.patchClient(
                              filteredNode,
                              entity,
                              ForestClientEntity.class,
                              mapper
                          )
                      )
                      .map(client -> client
                          .withUpdatedBy(userId)
                          .withUpdatedAt(LocalDateTime.now())
                          .withRevision(client.getRevision() + 1)
                      )
                      .filter(client -> !entity.equals(client))
                      //Can only happen if there's a change
                      .map(client ->
                          client
                              .withUpdatedAt(LocalDateTime.now())
                              .withUpdatedBy(userId) // Is still missing the user org unit
                              .withRevision(client.getRevision() + 1)
                      )
                      .doOnNext(client -> log.info("Applying Forest Client changes {}", client))
              )
              .flatMap(clientRepository::save)
              .then();
    }

    return Mono.empty();
  }
}
