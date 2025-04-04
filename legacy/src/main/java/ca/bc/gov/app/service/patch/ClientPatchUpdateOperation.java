package ca.bc.gov.app.service.patch;

import ca.bc.gov.app.util.PatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.function.Function;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClientPatchUpdateOperation<T> extends ClientPatchOperation {

  Map<String, String> getFieldToDataField();

  Function<T, Map<String, Object>> getExtraFields(String userId);

  Mono<T> findEntity(String clientNumber, String entityId);

  Query getEntityIdentification(String clientNumber, String entityId);

  Class<T> getEntityClass();

  default Mono<Void> applyReplacePatch(
      String clientNumber,
      JsonNode patch,
      ObjectMapper mapper,
      String userId,
      R2dbcEntityOperations entityTemplate
  ) {
    JsonNode filteredNodeOps = PatchUtils.filterOperationsByOp(
        patch,
        "replace",
        getPrefix(),
        getRestrictedPaths(),
        mapper
    );

    return Flux
        .fromIterable(PatchUtils.loadIds(filteredNodeOps))
        .flatMap(entityId ->
            findEntity(clientNumber, entityId)
                .flatMap(entity ->
                    Mono
                        .just(PatchUtils.filterById(filteredNodeOps, mapper).apply(entityId))
                        .map(node -> PatchUtils.filterPatchOperations(
                                node,
                                entityId,
                                getRestrictedPaths(),
                                mapper
                            )
                        )
                        .map(node -> PatchUtils.buildUpdate(node, getFieldToDataField(),
                            getExtraFields(userId).apply(entity)))
                        .filter(updateMapper -> !updateMapper.isEmpty())
                        .map(Update::from)
                        .flatMap(executeUpdate(
                                getEntityIdentification(clientNumber, entityId),
                                entityTemplate
                            )
                        )
                )
        )
        .then();
  }

  default Function<Update, Mono<Long>> executeUpdate(
      Query whereCondition,
      R2dbcEntityOperations entityTemplate
  ) {
    return update -> entityTemplate.update(whereCondition, update, getEntityClass());
  }

}
