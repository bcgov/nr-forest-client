package ca.bc.gov.app.service.patch;

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
@Order(5)
public class PatchOperationContactRemoveService implements ClientPatchOperation {

  private static final String REMOVE_ALL_CONTACTS = """
      DELETE FROM THE.CLIENT_CONTACT 
      WHERE CLIENT_NUMBER = :client_number
      AND CONTACT_NAME = (SELECT cl.CONTACT_NAME FROM THE.CLIENT_CONTACT cl WHERE cl.CLIENT_CONTACT_ID=:entity_id)""";

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
            .map(node -> node.get("path").asText().replace("/", StringUtils.EMPTY))
            .map(Long::parseLong)
            .flatMap(entityId -> removeAllByEntityId(clientNumber, entityId))
            .then();

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
