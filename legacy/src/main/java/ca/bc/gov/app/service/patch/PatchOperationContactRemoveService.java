package ca.bc.gov.app.service.patch;

import ca.bc.gov.app.exception.ContactInUseException;
import ca.bc.gov.app.repository.ScaleSiteContactRepository;
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

  private static final String REMOVE_ALL_CONTACTS = """
      DELETE FROM THE.CLIENT_CONTACT 
      WHERE CLIENT_NUMBER = :client_number
      AND CONTACT_NAME = (
          SELECT cl.CONTACT_NAME FROM THE.CLIENT_CONTACT cl WHERE cl.CLIENT_CONTACT_ID = :entity_id
      )""";

  private final R2dbcEntityOperations entityTemplate;
  private final ScaleSiteContactRepository scaleSiteContactRepository;

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
                verifyNotInUse(entityId)
                    .then(removeAllByEntityId(clientNumber, entityId))
            )
            .then();

  }

  /**
   * Verifies that the contact identified by {@code entityId} is not currently being used by
   * another system (e.g. EMS, GAS2, LEXIS, or SCS) before allowing it to be deleted.
   *
   * @param entityId the client contact id to verify
   * @return a {@link Mono} that completes successfully if the contact can be deleted, or errors
   *     with {@link ContactInUseException} if the contact is still in use
   */
  private Mono<Void> verifyNotInUse(Long entityId) {
    return
        scaleSiteContactRepository
            .existsByClientContactId(entityId)
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
