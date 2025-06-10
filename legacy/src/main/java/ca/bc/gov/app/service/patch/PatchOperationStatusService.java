package ca.bc.gov.app.service.patch;

import static ca.bc.gov.app.util.QueryUtils.repeatAndLog;

import ca.bc.gov.app.dto.FieldReasonDto;
import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.repository.ClientUpdateReasonRepository;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@Observed
@RequiredArgsConstructor
@Order(4)
public class PatchOperationStatusService implements ClientPatchOperation {

  private final ForestClientRepository clientRepository;
  private final ClientUpdateReasonRepository clientUpdateReasonRepository;

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
    return List.of("/clientStatusCode");
  }

  @Override
  public Mono<Void> applyPatch(
      String clientNumber,
      JsonNode patch,
      ObjectMapper mapper,
      String userName
  ) {
    if (!PatchUtils.checkOperation(patch, getPrefix(), mapper)) {
      return Mono.empty();
    }
    return patchClient(clientNumber, patch, mapper, userName);
  }

  private Mono<Void> patchClient(String clientNumber, JsonNode patch, ObjectMapper mapper,
      String userName) {
    if (PatchUtils.checkOperation(patch, getPrefix(), mapper)) {
      JsonNode filteredNode = PatchUtils.filterPatchOperations(
          patch,
          getPrefix(),
          getRestrictedPaths(),
          mapper
      );

      return clientRepository
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
                  //Can only happen if there's a change
                  .map(client ->
                      client
                          .withUpdatedAt(LocalDateTime.now())
                          .withUpdatedBy(userName) // Is still missing the user org unit
                          .withRevision(client.getRevision() + 1)
                  )
                  .doOnNext(
                      client -> log.info("Applying Forest Client changes to {}", clientNumber))
          )
          .flatMap(clientRepository::save)
          .flatMap(entity -> patchReason(clientNumber, patch, mapper, userName))
          .then();
    }
    return Mono.empty();
  }

  private Mono<Void> patchReason(String clientNumber, Object patch, ObjectMapper mapper,
      String userName) {

    JsonNode filteredNode = PatchUtils.filterOperationsByOp(
        patch,
        "add",
        "reasons",
        mapper
    );

    return
        Flux
            .fromIterable(filteredNode)
            .map(op -> PatchUtils.loadAddValue(op, FieldReasonDto.class, mapper))
            .filter(reason -> reason.field().equals("/client/clientStatusCode"))
            .doOnNext(
                op -> log.info("Client {} updated field {} due to {}", clientNumber, op.field(),
                    op.reason()))
            .flatMap(opValue -> updateReasonCode(clientNumber, opValue.reason()))
            .collectList()
            .then();
  }

  private Mono<Void> updateReasonCode(String clientNumber, String reasonCode) {
    return
        Mono.defer(() -> clientUpdateReasonRepository
                .findByNumberAndStatusCode(clientNumber))
            .repeatWhenEmpty(5, repeatAndLog("STATUS"))
            .map(reason -> reason.withUpdateReasonCode(reasonCode))
            .doOnNext(reason -> log.info("Reason code applied to {} as {}", clientNumber,
                reason.getUpdateReasonCode()))
            .flatMap(clientUpdateReasonRepository::save)
            .then();
  }
}
