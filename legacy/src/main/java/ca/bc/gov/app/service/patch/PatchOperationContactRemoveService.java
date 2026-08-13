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

/**
 * Patch operation that handles the removal of client contacts.
 *
 * <p>Removing a contact deletes every {@code CLIENT_CONTACT} row of the client that shares the
 * same {@code CONTACT_NAME}, as a single contact can be associated to multiple locations. Before
 * deleting, it validates that none of those rows are referenced by another system.</p>
 *
 * <p>When a single patch removes multiple contacts, every contact is validated first, in order,
 * before any deletion is attempted. This avoids leaving the client in a partially-updated state
 * when an earlier contact is successfully removable but a later one is still in use: in that
 * case the whole operation fails with {@link ContactInUseException} and nothing is deleted.</p>
 */
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
            .collectList()
            .flatMap(entityIds -> verifyNoneInUse(clientNumber, entityIds)
                .thenMany(removeAll(clientNumber, entityIds))
                .then()
            );
  }

  /**
   * Verifies, in order, that none of the contacts identified by {@code entityIds} (nor any other
   * contact of the client sharing the same {@code CONTACT_NAME}) are currently being used by
   * another system (e.g. EMS, GAS2, LEXIS, or SCS).
   *
   * <p>Checks run sequentially via {@code concatMap} and stop at the first contact found in use,
   * so that a later failure cannot occur after an earlier contact has already been deleted: this
   * method never deletes anything, it only validates the full set of contacts up front.</p>
   *
   * @param clientNumber the client number that owns the contacts
   * @param entityIds the client contact ids to verify, in the order they appear in the patch
   * @return a {@link Mono} that completes successfully if every contact can be deleted, or errors
   *     with {@link ContactInUseException} on the first one that is still in use
   */
  private Mono<Void> verifyNoneInUse(String clientNumber, List<Long> entityIds) {
    return
        Flux.fromIterable(entityIds)
            .concatMap(entityId -> verifyNotInUse(clientNumber, entityId))
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
            .doOnNext(count ->
                log.info("Contact {} in use by another system? {}", entityId, count > 0)
            )
            .flatMap(count -> count > 0
                ? Mono.error(new ContactInUseException())
                : Mono.empty()
            );
  }

  /**
   * Removes every contact identified by {@code entityIds}, in order. Only invoked after
   * {@link #verifyNoneInUse(String, List)} has confirmed that all of them can be safely deleted.
   *
   * @param clientNumber the client number that owns the contacts
   * @param entityIds the client contact ids to remove, in the order they appear in the patch
   * @return a {@link Flux} emitting each removed contact id, when available
   */
  private Flux<Long> removeAll(String clientNumber, List<Long> entityIds) {
    return
        Flux.fromIterable(entityIds)
            .concatMap(entityId -> removeAllByEntityId(clientNumber, entityId));
  }

  /**
   * Removes every contact of the client that shares the same {@code CONTACT_NAME} as the contact
   * identified by {@code entityId}.
   *
   * @param clientNumber the client number that owns the contacts
   * @param entityId the client contact id used to resolve the contact name
   * @return a {@link Mono} emitting the removed contact id, when available
   */
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