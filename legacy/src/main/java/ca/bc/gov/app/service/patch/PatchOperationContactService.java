package ca.bc.gov.app.service.patch;

import ca.bc.gov.app.ApplicationConstants;
import ca.bc.gov.app.dto.ForestClientContactDetailsDto;
import ca.bc.gov.app.dto.ForestClientContactDto;
import ca.bc.gov.app.entity.ForestClientContactEntity;
import ca.bc.gov.app.service.ClientContactService;
import ca.bc.gov.app.util.PatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.StreamSupport;
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

  public static final String LOAD_CONTACT_QUERY = """
      SELECT
        CLIENT_CONTACT_ID
      FROM THE.CLIENT_CONTACT
      WHERE
        CLIENT_NUMBER = :client_number
        AND
        CONTACT_NAME = (
          SELECT CONTACT_NAME FROM CLIENT_CONTACT WHERE CLIENT_CONTACT_ID = :contact_id
        )""";
  private final R2dbcEntityOperations entityTemplate;
  private final ClientContactService contactService;

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

      log.info("Applying patch to client contact for client {}", clientNumber);

      return
          Flux.concat(
                  applyRemovePatch(clientNumber, patch, mapper, userId),
                  applyReplacePatch(clientNumber, patch, mapper, userId),
                  applyAddPatch(clientNumber, patch, mapper, userId)
              )
              .then();
    }

    return Mono.empty();
  }

  private Mono<Void> applyReplacePatch(
      String clientNumber,
      JsonNode patch,
      ObjectMapper mapper,
      String userId
  ) {

    JsonNode filteredNodeOps = PatchUtils.filterOperationsByOp(
        patch,
        "replace",
        getPrefix(),
        false,
        getRestrictedPaths(),
        mapper
    );

    Function<Long, Flux<Long>> loadContacts = entityId -> entityTemplate
        .getDatabaseClient()
        .sql(LOAD_CONTACT_QUERY)
        .bind("client_number", clientNumber)
        .bind("contact_id", entityId)
        .fetch()
        .all()
        .map(mapping -> mapping.get("CLIENT_CONTACT_ID"))
        .map(clientContactId -> Long.parseLong(clientContactId.toString()));

    return
        Flux
            .fromStream(
                StreamSupport.stream(
                    filteredNodeOps.spliterator(),
                    false
                )
            )
            .flatMap(node ->
                Mono
                    .just(PatchUtils.loadId(node))
                    .map(Long::parseLong)
                    .flatMapMany(loadContacts)
                    .map(entityId -> PatchUtils.duplicateNodeWithId(node, entityId.toString()))
            )
            .reduce(PatchUtils.mergeNodes())
            .flatMap(readyNode -> applyReplacePatch(
                clientNumber,
                readyNode,
                mapper,
                userId,
                entityTemplate
            ));

  }

  private Mono<Void> applyAddPatch(
      String clientNumber,
      JsonNode patch,
      ObjectMapper mapper,
      String userId
  ) {
    JsonNode filteredNodeOps = PatchUtils.filterOperationsByOp(
        patch,
        "add",
        getPrefix(),
        mapper
    );

    return
        Flux
            .fromStream(
                StreamSupport
                    .stream(filteredNodeOps.spliterator(), false)
            )
            .map(entry ->
                PatchUtils.loadAddValue(entry, ForestClientContactDetailsDto.class, mapper)
            )
            .map(dto ->
                dto
                    .locationCodes()
                    .stream()
                    .map(locationCode ->
                        new ForestClientContactDto(
                            dto.clientNumber(),
                            locationCode,
                            List.of(),
                            dto.contactTypeCode(),
                            dto.contactName().toUpperCase(),
                            dto.businessPhone(),
                            dto.secondaryPhone(),
                            dto.faxNumber(),
                            dto.emailAddress().toUpperCase(),
                            userId,
                            userId,
                            70L
                        )
                    )
                    .toList()
            )
            .flatMapIterable(Function.identity())
            .flatMap(contactService::saveAndGetIndex)
            .then();
  }

  private Mono<Void> applyRemovePatch(
      String clientNumber,
      JsonNode patch,
      ObjectMapper mapper,
      String userId
  ) {
    return Mono.empty();
  }

  private Mono<Long> getNextContactId() {
    return entityTemplate
        .getDatabaseClient()
        .sql("SELECT THE.client_contact_seq.NEXTVAL FROM dual")
        .fetch()
        .first()
        .map(row -> row.get("NEXTVAL"))
        .map(value -> Long.valueOf(value.toString()));
  }

}
