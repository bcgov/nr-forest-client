package ca.bc.gov.app.service.patch;

import ca.bc.gov.app.ApplicationConstants;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Service responsible for applying **JSON Patch** operations to a forest client.
 * <p>
 * This service processes a JSON Patch request by delegating the update to a list of
 * {@link ClientPatchOperation} implementations. Each registered operation is given a chance to
 * apply changes based on the provided patch data.
 * </p>
 */
@Service
@Slf4j
@Observed
public class ClientPatchService {

  private final ObjectMapper mapper;
  private final List<ClientPatchOperation> partialServices;
  private final TransactionalOperator transactionalOperator;

  public ClientPatchService(
      ObjectMapper mapper,
      List<ClientPatchOperation> partialServices,
      ReactiveTransactionManager transactionManager
  ) {
    this.mapper = mapper;
    this.partialServices = partialServices;
    this.transactionalOperator = TransactionalOperator.create(transactionManager);
  }

  /**
   * Applies the JSON Patch updates to a forest client by invoking all available
   * {@link ClientPatchOperation} services within a single reactive transaction.
   * <p>
   * Each service in {@code partialServices} attempts to apply the patch if relevant. The updates
   * are chained reactively, ensuring all applicable patches are processed before completion.
   * </p>
   *
   * @param clientNumber The unique identifier of the forest client being updated.
   * @param forestClient The JSON Patch document describing the modifications.
   * @param userId       The user that requested the patch update
   * @return A {@link Mono} that completes when all patches have been applied.
   */
  public Mono<Void> patchClient(
      String clientNumber,
      JsonNode forestClient,
      String userId
  ) {
    log.info("Patching client with client number {} if any changes are detected {}", clientNumber,
        forestClient);

    String effectiveUserId = StringUtils.defaultIfBlank(userId, ApplicationConstants.DEFAULT_USER_ID);
    if (StringUtils.isBlank(userId)) {
      log.warn("Patch request for client {} has missing or blank userId; falling back to default user ID '{}'",
          clientNumber, effectiveUserId);
    }

    return Flux
        .fromStream(partialServices.stream())
        .concatMap(service -> service.applyPatch(clientNumber, forestClient, mapper, effectiveUserId))
        .then()
        .as(transactionalOperator::transactional);
  }
}
