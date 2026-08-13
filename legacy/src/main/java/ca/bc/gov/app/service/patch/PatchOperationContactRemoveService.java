package ca.bc.gov.app.service.patch;

import ca.bc.gov.app.exception.ContactInUseException;
import ca.bc.gov.app.repository.ForestClientQueries;
import ca.bc.gov.app.util.PatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@Observed
@RequiredArgsConstructor
@Order(7)
public class PatchOperationContactRemoveService implements ClientPatchOperation {

  private static final String REMOVE_ALL_CONTACTS = ForestClientQueries.REMOVE_ALL_CONTACTS;

  private static final String COUNT_CONTACTS_IN_USE = ForestClientQueries.COUNT_CONTACTS_IN_USE;

  private final R2dbcEntityOperations entityTemplate;

  @Override
  public String getPrefix() {
    return "contacts";
  }

  @Override
  public List<String> getRestrictedPaths() {
    return List.of();
  }

  @Override
  public Mono<Void> applyPatch(
      String clientNumber,
      JsonNode patch,
      ObjectMapper mapper,
      String userId
  ) {

    JsonNode filteredNodeOps = PatchUtils.filterOperationsByOp(
        patch,
        "remove",
        getPrefix(),
        List.of(),
        mapper
    );

    return
        Flux.fromStream(
                StreamSupport.stream(
                    filteredNodeOps.spliterator(),
                    false
                )
            )
            .filter(node -> !node.get("path").asText().contains("locationCodes"))
            .map(node -> node.get("path").asText().replace("/", StringUtils.EMPTY))
            .map(Long::parseLong)
            .flatMap(entityId ->
                verifyNotInUse(clientNumber, entityId)
                    .then(removeAllByEntityId(clientNumber, entityId))
            )
            .then();

  }

  /**
   * Verifies that none of the contacts that would be removed (all contacts of the client sharing
   * the same CONTACT_NAME as {@code entityId}) are currently being used by another system (e.g.
   * EMS, GAS2, LEXIS, or SCS) before allowing the deletion.
   *
   * @param clientNumber the client number that owns the contacts
   * @param entityId the client contact id to verify
   * @return a {@link Mono} that completes successfully if the contacts can be deleted, or errors
   *     with {@link ContactInUseException} if any of them is still in use
   */
  private Mono<Void> verifyNotInUse(String clientNumber, Long entityId) {
    return
        entityTemplate
            .getDatabaseClient()
            .sql(COUNT_CONTACTS_IN_USE)
            .bind("client_number", clientNumber)
            .bind("entity_id", entityId)
            .fetch()
            .one()
            .map(results -> results.get("IN_USE_COUNT"))
            .map(Object::toString)
            .map(Long::parseLong)
            .defaultIfEmpty(0L)
            .map(count -> count > 0)
            .doOnNext(inUse ->
                log.info("Contact {} in use by another system? {}", entityId, inUse)
            )
            .flatMap(inUse -> inUse
                ? Mono.error(new ContactInUseException())
                : Mono.empty()
            );
  }

  private Mono<Long> removeAllByEntityId(String clientNumber, Long entityId) {
    return
        entityTemplate
            .getDatabaseClient()
            .sql(REMOVE_ALL_CONTACTS)
            .bind("client_number", clientNumber)
            .bind("entity_id", entityId)
            .fetch()
            .one()
            .map(results -> results.get("CLIENT_CONTACT_ID"))
            .map(Object::toString)
            .map(Long::parseLong);

  }
}
