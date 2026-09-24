package ca.bc.gov.app.service.patch;

import static ca.bc.gov.app.ApplicationConstants.DEFAULT_PAGE_SIZE;

import ca.bc.gov.app.dto.ClientDoingBusinessAsDto;
import ca.bc.gov.app.entity.ClientDoingBusinessAsEntity;
import ca.bc.gov.app.repository.ClientDoingBusinessAsRepository;
import ca.bc.gov.app.service.ClientDoingBusinessAsService;
import ca.bc.gov.app.util.PatchUtils;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Patch operation that handles the client "doing business as" name.
 */
@Service
@Slf4j
@Observed
@Order(11)
public class PatchOperationDoingBusinessService implements ClientPatchOperation {

  public static final String DEFAULT_USER_ID = "idir\\ottomated";
  private static final String PATCH_VALUE_FIELD = "value";

  private final ClientDoingBusinessAsRepository dbaRepository;
  private final ClientDoingBusinessAsService service;
  private final TransactionalOperator transactionalOperator;

  /**
   * Constructs a new {@link PatchOperationDoingBusinessService}.
   *
   * @param dbaRepository the repository for managing DBA entities
   * @param service the service for saving and indexing DBA records
   * @param transactionManager the reactive transaction manager used for transactional operations
   */
  public PatchOperationDoingBusinessService(
      ClientDoingBusinessAsRepository dbaRepository,
      ClientDoingBusinessAsService service,
      ReactiveTransactionManager transactionManager
  ) {
    this.dbaRepository = dbaRepository;
    this.service = service;
    this.transactionalOperator = TransactionalOperator.create(transactionManager);
  }

  /**
   * Returns the prefix associated with this patch operation.
   *
   * @return The string prefix "doingBusinessAs".
   */
  @Override
  public String getPrefix() {
    return "doingBusinessAs";
  }

  /**
   * Returns a list of paths that are restricted from modification.
   *
   * @return A list of restricted JSON Patch paths.
   */
  @Override
  public List<String> getRestrictedPaths() {
    return List.of();
  }

  /**
   * Applies the JSON Patch operation to the client's doing business as record.
   *
   * @param clientNumber The unique identifier of the client to be patched.
   * @param patch The JSON Patch document describing the changes.
   * @param mapper The {@link ObjectMapper} used to deserialize and apply the patch.
   * @param userId The username of the user who triggered the request.
   * @return A {@link Mono} that completes when the patch has been applied.
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

      if (filteredNode.isEmpty()) {
        return Mono.empty();
      }

      if (isDeleteOperation(filteredNode)) {
        return handleRemove(clientNumber);
      }

      String dbaName = getDoingBusinessAsName(filteredNode);
      String effectiveUserId = StringUtils.defaultIfBlank(userId, DEFAULT_USER_ID);

      return dbaRepository
          .findByClientNumber(clientNumber)
          .doOnNext(dba -> log.info("Found DBAs for client {} as {}", clientNumber,
              dba.getDoingBusinessAsName()))
          .map(dba ->
              dba
                  .withDoingBusinessAsName(dbaName)
                  .withUpdatedBy(effectiveUserId)
                  .withUpdatedByUnit(DEFAULT_PAGE_SIZE) // We use 70 as the default org unit
                  .withUpdatedAt(LocalDateTime.now())
                  .withRevision(dba.getRevision() + 1)
          )
          .next()
          .flatMap(dbaRepository::save)
          //This is just a trick to have same return type as the switchIfEmpty below
          .map(ClientDoingBusinessAsEntity::getDoingBusinessAsName)
          .switchIfEmpty(
              Mono
                  .just(
                      new ClientDoingBusinessAsDto(
                          clientNumber,
                          dbaName,
                          effectiveUserId,
                          effectiveUserId,
                          DEFAULT_PAGE_SIZE //We use 70 as the default org unit
                      )
                  )
                  .doOnNext(dba -> log.info("No DBAs found for client {}, creating one as {}",
                      dba.clientNumber(), dba.doingBusinessAsName()))
                  .flatMap(service::saveAndGetIndex)
          )
          .then();

    }

    return Mono.empty();
  }

  /**
   * Removes every "doing business as" name of the client.
   *
   * @param clientNumber the client number that owns the names
   * @return a {@link Mono} that completes once every name has been removed
   */
  private Mono<Void> handleRemove(String clientNumber) {
    log.info("Removing DBA for client {}", clientNumber);
    return dbaRepository
        .findByClientNumber(clientNumber)
        .concatMap(dba ->
            dbaRepository
                .delete(dba)
                .doOnSuccess(unused -> log.info(
                    "Deleted DBA {} for client {}",
                    dba.getDoingBusinessAsName(),
                    clientNumber
                ))
        )
        .then()
        .as(transactionalOperator::transactional);
  }

  /**
   * Checks whether the patch represents a delete or removal operation.
   *
   * @param patch The JSON Patch node to inspect.
   * @return {@code true} if the operation removes or clears doing business as, {@code false} otherwise.
   */
  private boolean isDeleteOperation(JsonNode patch) {
    if (patch.isArray() && patch.has(0)) {
      JsonNode opNode = patch.get(0);
      String op = opNode.path("op").asText();
      if ("remove".equalsIgnoreCase(op)
          && opNode.path("path").asText().isEmpty()) {
        return true;
      }
      if (opNode.has(PATCH_VALUE_FIELD)) {
        JsonNode valueNode = opNode.get(PATCH_VALUE_FIELD);
        return valueNode.isNull() || StringUtils.isBlank(valueNode.asText());
      }
    }
    return false;
  }

  /**
   * Extracts and validates the doing business as name from the patch.
   *
   * @param patch The JSON Patch node containing the value.
   * @return The normalized uppercase doing business as name.
   * @throws IllegalArgumentException If the patch does not contain a non-blank value.
   */
  private String getDoingBusinessAsName(JsonNode patch) {
    if (patch.isArray() && patch.has(0) && patch.get(0).hasNonNull(PATCH_VALUE_FIELD)) {
      String name = patch.get(0).get(PATCH_VALUE_FIELD).asText().trim().toUpperCase(Locale.ROOT);
      if (StringUtils.isNotBlank(name)) {
        return name;
      }
    }
    log.error("Invalid patch format: expected an array with a non-blank 'value' field");
    throw new IllegalArgumentException("No value provided for doing business as");
  }
}
