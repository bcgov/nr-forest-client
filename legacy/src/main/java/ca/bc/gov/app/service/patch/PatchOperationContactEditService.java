package ca.bc.gov.app.service.patch;

import ca.bc.gov.app.ApplicationConstants;
import ca.bc.gov.app.entity.ForestClientContactEntity;
import ca.bc.gov.app.util.PatchUtils;
import ca.bc.gov.app.util.ReplacePatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@Observed
@RequiredArgsConstructor
@Order(8)
public class PatchOperationContactEditService implements ClientPatchOperation {

  private static final String GET_ALL_CONTACT_IDS = """
      SELECT CLIENT_CONTACT_ID FROM CLIENT_CONTACT
      WHERE
      	CLIENT_NUMBER = :client_number
      	AND CONTACT_NAME = (SELECT cl.CONTACT_NAME FROM THE.CLIENT_CONTACT cl WHERE cl.CLIENT_CONTACT_ID=:entity_id)""";

  private final R2dbcEntityOperations entityTemplate;

  private final Map<String, String> fieldToDataField = Map.of(
      "/contactName", "contact_name",
      "/contactTypeCode", "bus_contact_code",
      "/emailAddress", "email_address",
      "/faxNumber", "fax_number",
      "/secondaryPhone", "cell_phone",
      "/businessPhone", "business_phone"
  );

  @Override
  public String getPrefix() {
    return "contacts";
  }

  @Override
  public List<String> getRestrictedPaths() {
    return List.of(
        "/contactName",
        "/contactTypeCode",
        "/businessPhone",
        "/secondaryPhone",
        "/faxNumber",
        "/emailAddress"
    );
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
        "replace",
        getPrefix(),
        getRestrictedPaths(),
        mapper
    );

    return
        // Load ids
        Flux
            .fromIterable(PatchUtils.loadIds(filteredNodeOps))
            .filter(StringUtils::isNumeric)
            .flatMap(entityId ->
                Mono.just(entityId)
                    // Get changes for just that ID
                    .map(PatchUtils.filterById(filteredNodeOps, mapper))
                    // Filter Patches
                    .map(node -> PatchUtils.filterPatchOperations(
                            node,
                            entityId,
                            getRestrictedPaths(),
                            mapper
                        )
                    )
                    .flatMap(node ->
                        // Load the entity JUST BECAUSE OF REVISION
                        findEntity(clientNumber, Long.parseLong(entityId))
                            // Generate update
                            .map(entity -> ReplacePatchUtils.buildUpdate(
                                    node,
                                    fieldToDataField,
                                    getExtraFields(userId, entity.getRevision() + 1)
                                )
                            )
                    )
                    // Turn the map into Update
                    .map(Update::from)
                    .flatMap(update ->
                        // Get all ids related to that entry
                        getAllEntityIds(clientNumber, Long.parseLong(entityId))
                            .doOnNext(entityIds -> log.info("Updating contacts {} with the following changes {}",entityIds,update))
                            .flatMap(entityIds ->
                                // Update all at once
                                entityTemplate.update(
                                    getEntityIdentification(clientNumber, entityIds),
                                    update,
                                    getEntityClass()
                                )
                            )
                    )
            )
            .then();
  }


  private Map<String, Object> getExtraFields(String userId, long revision) {
    return Map.of(
        "update_timestamp", LocalDateTime.now(),
        "update_userid", userId,
        "update_org_unit", 70L,
        "revision_count", revision
    );
  }

  private Mono<ForestClientContactEntity> findEntity(String clientNumber, Long entityId) {
    return entityTemplate
        .selectOne(
            getEntityIdentification(clientNumber, List.of(entityId)),
            getEntityClass()
        );
  }

  private Query getEntityIdentification(String clientNumber, List<Long> entityIds) {
    return Query
        .query(
            Criteria
                .where("client_contact_id").in(entityIds)
                .and(ApplicationConstants.CLIENT_NUMBER).is(clientNumber)
        );
  }

  private Class<ForestClientContactEntity> getEntityClass() {
    return ForestClientContactEntity.class;
  }

  private Mono<List<Long>> getAllEntityIds(String clientNumber, Long entityId) {
    return
        entityTemplate
            .getDatabaseClient()
            .sql(GET_ALL_CONTACT_IDS)
            .bind("client_number", clientNumber)
            .bind("entity_id", entityId)
            .fetch()
            .all()
            .map(results -> results.get("CLIENT_CONTACT_ID"))
            .map(Object::toString)
            .map(Long::parseLong)
            .collectList();

  }

}
