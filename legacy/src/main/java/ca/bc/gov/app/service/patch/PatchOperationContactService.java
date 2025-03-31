package ca.bc.gov.app.service.patch;

import ca.bc.gov.app.ApplicationConstants;
import ca.bc.gov.app.entity.ForestClientContactEntity;
import ca.bc.gov.app.util.PatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@Observed
@RequiredArgsConstructor
@Order(5)
public class PatchOperationContactService implements
    ClientPatchUpdateOperation<ForestClientContactEntity> {

  private final R2dbcEntityOperations entityTemplate;

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
        "/emailAddress");
  }

  @Override
  public Map<String, String> getFieldToDataField() {
    return Map.of(
        "/contactName", "contact_name",
        "/contactTypeCode", "bus_contact_code",
        "/emailAddress", "email_address",
        "/faxNumber", "fax_number",
        "/secondaryPhone", "cell_phone",
        "/businessPhone", "business_phone"
    );
  }

  @Override
  public Function<ForestClientContactEntity, Map<String, Object>> getExtraFields(String userId) {
    return entity -> Map.of(
        "update_timestamp", LocalDateTime.now(),
        "update_userid", userId,
        "update_org_unit", 70L,
        "revision_count", entity.getRevision() + 1
    );
  }

  @Override
  public Mono<ForestClientContactEntity> findEntity(String clientNumber, String entityId) {
    return entityTemplate
        .selectOne(
            getEntityIdentification(clientNumber, entityId),
           getEntityClass()
        );
  }

  @Override
  public Query getEntityIdentification(String clientNumber, String entityId) {
    return Query
        .query(
            Criteria
                .where("client_contact_id").is(entityId)
                .and(ApplicationConstants.CLIENT_NUMBER).is(clientNumber)
        );
  }

  @Override
  public Class<ForestClientContactEntity> getEntityClass() {
    return ForestClientContactEntity.class;
  }

  @Override
  public Mono<Void> applyPatch(String clientNumber, JsonNode patch, ObjectMapper mapper,
      String userId) {
    // If there's a patch operation targeting client location data we move ahead
    if (PatchUtils.checkOperation(patch, getPrefix(), mapper)) {

      log.info("Contact change, way to go buddy {}", patch);

      return
          Flux.concat(
                  applyRemovePatch(clientNumber, patch, mapper, userId),
                  applyReplacePatch(clientNumber, patch, mapper, userId, entityTemplate),
                  applyAddPatch(clientNumber, patch, mapper, userId)
              )
              .then();
    }

    return Mono.empty();
  }

  private Mono<Void> applyAddPatch(
      String clientNumber,
      JsonNode patch,
      ObjectMapper mapper,
      String userId
  ) {
    return Mono.empty();
  }

  private Mono<Void> applyRemovePatch(
      String clientNumber,
      JsonNode patch,
      ObjectMapper mapper,
      String userId
  ) {
    return Mono.empty();
  }

}
