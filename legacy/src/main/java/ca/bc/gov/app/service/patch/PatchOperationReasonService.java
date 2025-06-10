package ca.bc.gov.app.service.patch;

import static ca.bc.gov.app.util.QueryUtils.repeatAndLog;
import static java.util.function.Predicate.not;

import ca.bc.gov.app.dto.FieldReasonDto;
import ca.bc.gov.app.entity.ClientUpdateReasonEntity;
import ca.bc.gov.app.entity.ForestClientEntity;
import ca.bc.gov.app.repository.ClientUpdateReasonRepository;
import ca.bc.gov.app.repository.ForestClientRepository;
import ca.bc.gov.app.util.PatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public abstract class PatchOperationReasonService implements ClientPatchOperation {

  private final ForestClientRepository clientRepository;
  private final ClientUpdateReasonRepository clientUpdateReasonRepository;

  /**
   * Returns the field name associated with this patch operation inside the reasons. It is used as
   * a filter to determine which field's reasons should be updated.
   *
   * @return The string with the field path.
   */
  abstract String getFieldName();

  /**
   * Returns the identifiable reason ID for retry and logging purposes.
   * @return The string reason ID.
   */
  abstract String getReasonId();

  /**
   * Returns the reason code used to filter the reason entries in the database. If null is provided,
   * the search will be done only by client number.
   * @return The string reason type or null.
   */
  abstract String getReasonType();

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

  private Mono<Void> patchClient(
      String clientNumber,
      JsonNode patch,
      ObjectMapper mapper,
      String userName
  ) {
    if (PatchUtils.checkOperation(patch, getPrefix(), mapper)) {
      JsonNode filteredNode = PatchUtils.filterPatchOperations(
          patch,
          getPrefix(),
          getRestrictedPaths(),
          mapper
      );

      return
          Mono
              .just(filteredNode)
              .filter(not(JsonNode::isEmpty))
              .flatMap(node ->
                  clientRepository
                      .findByClientNumber(clientNumber)
                      .flatMap(entity ->
                          Mono
                              .just(
                                  PatchUtils.patchClient(
                                      node,
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
                              .doOnNext(client -> log.info("Applying Forest Client changes to {}",
                                  clientNumber))
                      )
              )
              .flatMap(clientRepository::save)
              .flatMap(entity -> patchReason(
                      clientNumber,
                      patch,
                      mapper)
              )
              .then();
    }
    return Mono.empty();
  }

  /**
   * Applies patch operations to update the reason code for a specific field of a client.
   * <p>
   * This method filters the provided patch JSON for "add" operations on "reasons", deserializes
   * them into {@link FieldReasonDto} objects, and processes only those matching the given field
   * name. For each matching operation, it logs the update and invokes
   * {@link #updateReasonCode(String, String)} to update the reason code in the
   * database. The method returns a {@link Mono} that completes when all updates are processed.
   * </p>
   *
   * @param clientNumber the client number to update
   * @param patch        the JSON patch containing operations
   * @param mapper       the {@link ObjectMapper} for JSON deserialization
   * @return a {@link Mono} that completes when all relevant reason codes are updated
   */
  private Mono<Void> patchReason(
      String clientNumber,
      JsonNode patch,
      ObjectMapper mapper
  ) {

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
            .filter(reason -> reason.field().equals(getFieldName()))
            .doOnNext(
                op -> log.info("Client {} updated field {} due to {}", clientNumber, op.field(),
                    op.reason()))
            .flatMap(opValue -> updateReasonCode(
                    clientNumber,
                    opValue.reason()
                )
            )
            .collectList()
            .then();
  }

  /**
   * Updates the reason code for a client based on the provided parameters.
   * <p>
   * This method retrieves a {@link ClientUpdateReasonEntity} using the client number and reason
   * type, updates its reason code, logs the operation, saves the updated entity, and completes the
   * reactive chain. If the entity is not found, it retries the search up to 5 times using the
   * provided reason ID for logging.
   * </p>
   *
   * @param clientNumber the client number to update
   * @param reasonCode   the new reason code to set
   * @return a {@link Mono} that completes when the update is done
   */
  private Mono<Void> updateReasonCode(
      String clientNumber,
      String reasonCode
  ) {
    return
        Mono.defer(getSearchQuery(clientNumber, getReasonType()))
            .repeatWhenEmpty(5, repeatAndLog(getReasonId()))
            .map(reason -> reason.withUpdateReasonCode(reasonCode))
            .doOnNext(reason -> log.info("Reason code applied to {} as {}", clientNumber,
                reason.getUpdateReasonCode()))
            .flatMap(clientUpdateReasonRepository::save)
            .then();
  }

  /**
   * Returns a supplier based on the given client number and optional reason type. If reasonType is
   * not blank, it queries by both client number and reason type; otherwise, it queries by client
   * number only.
   *
   * @param clientNumber the client number to search for
   * @param reasonType   the reason type to filter by (optional)
   * @return a Supplier of {@link Mono} {@link ClientUpdateReasonEntity} for the search query
   */
  private Supplier<Mono<ClientUpdateReasonEntity>> getSearchQuery(
      String clientNumber,
      String reasonType
  ) {
    if (StringUtils.isNotBlank(reasonType)) {
      return () -> clientUpdateReasonRepository.findUndefinedByNumberWithFilteredActions(clientNumber, reasonType);
    }
    return () -> clientUpdateReasonRepository.findUndefinedByNumberWithFilteredActions(clientNumber);
  }
}
