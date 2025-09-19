package ca.bc.gov.app.service.patch;

import static java.util.function.Predicate.not;
import ca.bc.gov.app.ApplicationConstants;
import ca.bc.gov.app.dto.RelatedClientEntryDto;
import ca.bc.gov.app.entity.RelatedClientEntity;
import ca.bc.gov.app.repository.RelatedClientRepository;
import ca.bc.gov.app.util.PatchUtils;
import ca.bc.gov.app.util.ReplacePatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.observation.annotation.Observed;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.annotation.Order;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@Observed
@RequiredArgsConstructor
@Order(12)
public class PatchOperationsRelatedClientService implements ClientPatchOperation {

  private final R2dbcEntityOperations entityTemplate;
  private final RelatedClientRepository relatedClientRepository;

  // This regex pattern is used to extract two segments from a path.
  // It matches a string of the form "/{locationId}/{index}" and captures:
  // - Group 1: locationId (any sequence of characters except '/')
  // - Group 2: index (any sequence of characters except '/')
  private final Pattern pattern = Pattern.compile("/([^/]+)/([^/]+)");
  private final String CHECK_RELATION_EXIST = """
      SELECT
        count(1) as count_results
      FROM RELATED_CLIENT rc
      WHERE
        rc.CLIENT_NUMBER = :clientNumber
        AND rc.CLIENT_LOCN_CODE = :locnCode
        AND rc.RELATED_CLNT_NMBR = :relatedClient
        AND rc.RELATED_CLNT_LOCN = :relatedLocation
        AND rc.RELATIONSHIP_CODE = :relationship""";
  private final String UPDATE_DEL_USER = """
      UPDATE
        REL_CLI_AUDIT
      SET UPDATE_USERID = :userId
      WHERE
        UPDATE_USERID = 'USERTMP'
        AND CLIENT_AUDIT_CODE = 'DEL'""";

  private final Map<String, String> fieldToDataField = Map.of(
      "/client/location/code", "CLIENT_LOCN_CODE",
      "/relatedClient/location/code", "RELATED_CLNT_LOCN",
      "/relatedClient/client/code", "RELATED_CLNT_NMBR",
      "/relationship/code", "RELATIONSHIP_CODE",
      "/percentageOwnership", "PERCENT_OWNERSHIP",
      "/hasSigningAuthority", "SIGNING_AUTH_IND"
  );

  @Override
  public String getPrefix() {
    return "relatedClients";
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

    if (PatchUtils.checkOperation(patch, getPrefix(), mapper)) {
      return
          Flux.concat(
                  applyRemove(clientNumber, patch, mapper, userId),
                  applyReplace(clientNumber, patch, mapper, userId),
                  applyAdd(clientNumber, patch, mapper, userId)
              )
              .then();
    }

    return Mono.empty();
  }

  private Mono<Void> applyRemove(
      String clientNumber,
      JsonNode patch,
      ObjectMapper mapper,
      String userId
  ) {
    JsonNode filteredNodeOps = PatchUtils.filterOperationsByOp(
        patch,
        "remove",
        getPrefix(),
        mapper
    );

    if (filteredNodeOps.isEmpty()) {
      return Mono.empty();
    }
    log.info("Applying remove operations for related clients: {}", filteredNodeOps);

    return Flux
        .fromStream(StreamSupport.stream(filteredNodeOps.spliterator(), false))
        .map(entry -> entry.get("path").asText())
        //Generate a regex that allows me to split a string with the following format and grab both values: /00/1
        .map(this::extractLocationAndIndex)
        .filter(entry -> StringUtils.isNotBlank(entry.getKey()))
        .flatMap(entry -> processRemove(clientNumber, entry.getKey(), entry.getValue(), userId))
        .then();
  }

  private Mono<Void> applyReplace(
      String clientNumber,
      JsonNode patch,
      ObjectMapper mapper,
      String userId
  ) {
      JsonNode filteredNodeOps = PatchUtils.filterOperationsByOp(
          patch,
          "replace",
          getPrefix(),
          mapper
      );

      if (filteredNodeOps.isEmpty()) {
          return Mono.empty();
      }
      log.info("Applying replace operations for related clients: {}", filteredNodeOps);

      return Flux
          .fromIterable(PatchUtils.loadIdsAndSubIds(filteredNodeOps).entrySet())
          .flatMap(locationEntry ->
              Flux.fromStream(locationEntry.getValue().stream())
                  .flatMap(index ->
                      relatedClientRepository
                          .findByClientNumberAndClientLocationCode(
                              clientNumber,
                              locationEntry.getKey()
                          )
                          .elementAt(Integer.parseInt(index), new RelatedClientEntity())
                          .filter(not(RelatedClientEntity::isInvalid))
                          .flatMap(entity -> {
                              JsonNode nodeOpsForEntity = PatchUtils.filterOperationsByOp(
                                  filteredNodeOps,
                                  "replace",
                                  String.format("%s/%s", locationEntry.getKey(), index),
                                  getRestrictedPaths(),
                                  mapper
                              );
                              
                              nodeOpsForEntity.forEach(op -> {
                                  String path = op.get("path").asText();
                                  if (path.endsWith("/hasSigningAuthority")) {
                                      // Replace the value in the JSON node with "Y"/"N"
                                      ((ObjectNode) op).put(
                                          "value",
                                          BooleanUtils.toString(op.get("value").asBoolean(), "Y", "N", null)
                                      );
                                  }
                              });

                              Map<SqlIdentifier, Object> updateMap = ReplacePatchUtils.buildUpdate(
                                  nodeOpsForEntity,
                                  fieldToDataField,
                                  getExtraFields(userId, entity.getRevision() + 1)
                              );

                              log.info(
                                  "Applying replace operations for related client {} {} at location {} {}: {}",
                                  clientNumber, locationEntry,
                                  entity.getRelatedClientNumber(),
                                  entity.getRelatedClientLocationCode(),
                                  updateMap
                              );
                              
                              return entityTemplate
                                  .update(
                                      getCriteria(entity),
                                      Update.from(updateMap),
                                      RelatedClientEntity.class
                                  )
                                  .doOnNext(reached -> log.info(
                                      "Updated {} related client {} at location {} with new values",
                                      reached,
                                      entity.getRelatedClientNumber(),
                                      entity.getRelatedClientLocationCode()
                                  ));
                          })
                  )
          )
          .then();
  }

  private Mono<Void> applyAdd(
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

    if (filteredNodeOps.isEmpty()) {
      return Mono.empty();
    }
    log.info("Applying add operations for related clients: {}", filteredNodeOps);

    return
        Flux
            .fromStream(StreamSupport.stream(filteredNodeOps.spliterator(), false))
            .flatMap(entry ->
                processAdd(
                    clientNumber,
                    mapper,
                    entry.get("path").asText().replace("/", StringUtils.EMPTY).replace("null", StringUtils.EMPTY),
                    entry.get("value"),
                    userId
                )
            )
            .then();
  }

  private Mono<Void> processRemove(
      String clientNumber,
      String locationId,
      int index,
      String userId
  ) {
    return
        relatedClientRepository
            .findByClientNumberAndClientLocationCode(clientNumber, locationId)
            .elementAt(index, new RelatedClientEntity())
            .filter(not(RelatedClientEntity::isInvalid))
            .flatMap(entity ->
                entityTemplate
                    .delete(RelatedClientEntity.class)
                    .matching(getCriteria(entity))
                    .all()
            )
            .doOnNext(quantity ->
                log.info("Removed {} related client entries for client {} at location {}",
                    quantity, clientNumber, locationId
                )
            )
            .flatMap(quantity ->
                entityTemplate
                    .getDatabaseClient()
                    .sql(UPDATE_DEL_USER)
                    .bind("userId", userId)
                    .fetch()
                    .rowsUpdated()
                )
            .then();
  }

  private Mono<Void> processAdd(
      String clientNumber,
      ObjectMapper mapper,
      String locationId,
      JsonNode value,
      String userId
  ) {
    if (value != null) {
      if (value.isArray()) {
        return Flux
            //Grab all values because it's an array
            .fromStream(value.valueStream())
            .flatMap(jsonNode ->
                addRelatedEntry(
                    jsonNode,
                    mapper,
                    clientNumber,
                    locationId,
                    userId
                )
            )
            //Save each related client entry on the database
            .then();
      } else {
        return addRelatedEntry(
            value,
            mapper,
            clientNumber,
            locationId,
            userId
        )
            .then();
      }
    }

    return Mono.empty();
  }

  private Mono<RelatedClientEntity> addRelatedEntry(
      JsonNode value,
      ObjectMapper mapper,
      String clientNumber,
      String locationId,
      String userId
  ) {
    return Mono
        .just(value)
        .map(entry ->
            mapper
                .createObjectNode()
                .set("value", entry)
        )
        //Cast because of java type erasure
        .cast(JsonNode.class)
        //Convert to RelatedClientEntryDto
        .map(node2 -> PatchUtils.loadAddValue(node2, RelatedClientEntryDto.class,
            mapper))
        // Check if the relationship already exists, and if it does skip
        .filterWhen(dto ->
            hasRelationship(
                clientNumber,
                locationId,
                dto.relatedClient().client().code(),
                dto.relatedClient().location().code(),
                dto.relationship().code()
            )
                .map(found -> !found)
        )
        //Convert to entity
        //It is done in two steps because it's easier to track the operations and debug, has
        //the same effect as a single map and the performance impact is negligible
        .map(dto ->
            RelatedClientEntity
                .builder()
                .clientNumber(clientNumber)
                .clientLocationCode(locationId)
                .relatedClientNumber(dto.relatedClient().client().code())
                .relatedClientLocationCode(dto.relatedClient().location().code())
                .relationshipType(dto.relationship().code())
                .signingAuthInd(
                    BooleanUtils.toString(dto.hasSigningAuthority(), "Y", "N", null))
                .percentOwnership(
                    Optional.ofNullable(
                            dto.percentageOwnership()
                        )
                        .map(Float::doubleValue)
                        .map(BigDecimal::valueOf)
                        .orElse(null)
                )
                .createdAt(LocalDateTime.now())
                .createdBy(userId)
                .updatedAt(LocalDateTime.now())
                .updatedBy(userId)
                .updatedByUnit(70L)
                .createdByUnit(70L)
                .revision(1L)
                .build()
        )
        .flatMap(entity ->
            entityTemplate
                .insert(RelatedClientEntity.class)
                .using(entity)
        )
        .doOnNext(entity -> log.info(
            "Adding relationship between {} ({}) and {} ({})",
            clientNumber, locationId, entity.getRelatedClientNumber(),
            entity.getRelatedClientLocationCode())
        );
  }

  private Mono<Boolean> hasRelationship(
      String clientNumber,
      String locnCode,
      String relatedClient,
      String relatedLocation,
      String relationship
  ) {
    return entityTemplate
        .getDatabaseClient()
        .sql(CHECK_RELATION_EXIST)
        .bind("clientNumber", clientNumber)
        .bind("locnCode", locnCode)
        .bind("relatedClient", relatedClient)
        .bind("relatedLocation", relatedLocation)
        .bind("relationship", relationship)
        .fetch()
        .one()
        .map(row -> row.get("count_results"))
        .cast(BigDecimal.class)
        .map(count -> count != null && count.longValue() > 0);
  }

  private Map<String, Object> getExtraFields(String userId, long revision) {
    return Map.of(
        "update_timestamp", LocalDateTime.now(),
        "update_userid", userId,
        "update_org_unit", 70L,
        "revision_count", revision
    );
  }

  private Query getCriteria(RelatedClientEntity entity) {
    return Query
        .query(
            Criteria
                .where("CLIENT_LOCN_CODE").is(entity.getClientLocationCode())
                .and(ApplicationConstants.CLIENT_NUMBER).is(entity.getClientNumber())
                .and("RELATED_CLNT_NMBR").is(entity.getRelatedClientNumber())
                .and("RELATED_CLNT_LOCN").is(entity.getRelatedClientLocationCode())
                .and("RELATIONSHIP_CODE").is(entity.getRelationshipType())
        );
  }

  private Pair<String, Integer> extractLocationAndIndex(String path) {
    var matcher = pattern.matcher(path);
    if (matcher.find()) {
      String locationId = matcher.group(1);
      int index = Integer.parseInt(matcher.group(2));
      return Pair.of(locationId, index);
    }
    return Pair.of(null, null);
  }
}
