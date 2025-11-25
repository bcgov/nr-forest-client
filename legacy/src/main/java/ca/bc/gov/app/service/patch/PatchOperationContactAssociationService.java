package ca.bc.gov.app.service.patch;

import ca.bc.gov.app.dto.ContactAssociationDto;
import ca.bc.gov.app.util.PatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.function.Function;
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
@Order(10)
public class PatchOperationContactAssociationService implements ClientPatchOperation {

  private static final String GET_ALL_CONTACT_IDS = """
      SELECT CLIENT_LOCN_CODE FROM CLIENT_CONTACT
      WHERE
      	CLIENT_NUMBER = :client_number
      	AND CONTACT_NAME = (SELECT cl.CONTACT_NAME FROM THE.CLIENT_CONTACT cl WHERE cl.CLIENT_CONTACT_ID=:entity_id)
      ORDER BY CLIENT_LOCN_CODE""";

  private static final String CHECK_CONTACT_EXIST = """
      SELECT COUNT(1) as cnt
      FROM CLIENT_CONTACT
      WHERE
        CLIENT_NUMBER = :client_number
        AND CONTACT_NAME = (SELECT cl.CONTACT_NAME FROM THE.CLIENT_CONTACT cl WHERE cl.CLIENT_CONTACT_ID=:entity_id)
        AND CLIENT_LOCN_CODE = :location_code""";

  public static final String REMOVE_CONTACT_ASSOCIATION = """
      DELETE FROM THE.CLIENT_CONTACT WHERE
      CLIENT_NUMBER = :client_number
      AND CONTACT_NAME = (SELECT cl.CONTACT_NAME FROM THE.CLIENT_CONTACT cl WHERE cl.CLIENT_CONTACT_ID=:entity_id)
      AND CLIENT_LOCN_CODE = :location_code""";

  public static final String ADD_CONTACT_ASSOCIATION = """
      INSERT INTO THE.CLIENT_CONTACT
      (
        CLIENT_CONTACT_ID, CLIENT_NUMBER, CLIENT_LOCN_CODE, BUS_CONTACT_CODE,
        CONTACT_NAME, BUSINESS_PHONE, CELL_PHONE, FAX_NUMBER,
        EMAIL_ADDRESS, UPDATE_TIMESTAMP, UPDATE_USERID, UPDATE_ORG_UNIT,
        ADD_TIMESTAMP, ADD_USERID, ADD_ORG_UNIT, REVISION_COUNT
      )
      VALUES
      (client_contact_seq.NEXTVAL, :client_number, :location_code,
      (SELECT cl.BUS_CONTACT_CODE FROM THE.CLIENT_CONTACT cl WHERE cl.CLIENT_CONTACT_ID=:entity_id),
      (SELECT cl.CONTACT_NAME FROM THE.CLIENT_CONTACT cl WHERE cl.CLIENT_CONTACT_ID=:entity_id),
      (SELECT cl.BUSINESS_PHONE FROM THE.CLIENT_CONTACT cl WHERE cl.CLIENT_CONTACT_ID=:entity_id),
      (SELECT cl.CELL_PHONE FROM THE.CLIENT_CONTACT cl WHERE cl.CLIENT_CONTACT_ID=:entity_id),
      (SELECT cl.FAX_NUMBER FROM THE.CLIENT_CONTACT cl WHERE cl.CLIENT_CONTACT_ID=:entity_id),
      (SELECT cl.EMAIL_ADDRESS FROM THE.CLIENT_CONTACT cl WHERE cl.CLIENT_CONTACT_ID=:entity_id),
      SYSDATE, :user_id, 70, SYSDATE, :user_id, 70, 1)""";

  public static final String REPLACE_CONTACT_ASSOCIATION = """
      UPDATE THE.CLIENT_CONTACT
      SET
        CLIENT_LOCN_CODE = :new_location_code,
        update_timestamp = SYSDATE,
        revision_count = revision_count + 1,
        update_userid = :user_id,
        update_org_unit = 70
      WHERE CLIENT_NUMBER = :client_number
      AND CONTACT_NAME = (SELECT cl.CONTACT_NAME FROM THE.CLIENT_CONTACT cl WHERE cl.CLIENT_CONTACT_ID=:entity_id)
      AND CLIENT_LOCN_CODE = :old_location_code""";

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

    return
        Flux.fromStream(StreamSupport.stream(patch.spliterator(), false))
            .filter(node -> node.has("path"))
            .filter(node -> node.get("path").asText().contains("locationCodes"))
            .flatMap(node ->
                getLocationCodeOrder(clientNumber, Long.parseLong(PatchUtils.loadId(node)))
                    .map(convertToAction(node))
            )
            .collectList()
            .flatMap(entries ->
                processRemove(clientNumber, entries)
                    .then(processAdd(clientNumber, userId, entries))
                    .then(processReplace(clientNumber, userId, entries))
            )
            .then();


  }


  private static Function<List<String>, ContactAssociationDto> convertToAction(JsonNode node) {
    return locations -> {
      if (node.get("op").asText().equals("replace")) {
        return new ContactAssociationDto(
            Long.parseLong(PatchUtils.loadId(node)),
            node.get("op").asText(),
            locations.get(getIndex(node)),
            node.get("value").asText()
        );
      }
      if (node.get("op").asText().equals("remove")) {
        return new ContactAssociationDto(
            Long.parseLong(PatchUtils.loadId(node)),
            node.get("op").asText(),
            locations.get(getIndex(node)),
            StringUtils.EMPTY
        );
      }
      return new ContactAssociationDto(
          Long.parseLong(PatchUtils.loadId(node)),
          node.get("op").asText(),
          StringUtils.EMPTY,
          node.get("value").asText()
      );

    };
  }

  private static int getIndex(JsonNode node) {
    return Integer.parseInt(node.get("path").asText().split("locationCodes/")[1]);
  }

  private Mono<List<String>> getLocationCodeOrder(String clientNumber, Long entityId) {
    return
        entityTemplate
            .getDatabaseClient()
            .sql(GET_ALL_CONTACT_IDS)
            .bind("client_number", clientNumber)
            .bind("entity_id", entityId)
            .fetch()
            .all()
            .map(result -> result.get("CLIENT_LOCN_CODE"))
            .map(String::valueOf)
            .collectList();

  }

  private Mono<Void> applyRemove(
      Long entityId,
      String locationCode,
      String clientNumber
  ) {
    return
        entityTemplate
            .getDatabaseClient()
            .sql(REMOVE_CONTACT_ASSOCIATION)
            .bind("client_number", clientNumber)
            .bind("entity_id", entityId)
            .bind("location_code", locationCode)
            .fetch()
            .one()
            .then();
  }

  private Mono<Void> processRemove(
      String clientNumber,
      List<ContactAssociationDto> entries
  ) {
    return Flux
        .fromIterable(entries)
        .filter(entry -> entry.operation().equals("remove"))
        .concatMap(entry -> applyRemove(
                entry.entityId(),
                entry.oldLocationCode(),
                clientNumber
            )
        )
        .then();
  }

  private Mono<Void> applyAdd(
      Long entityId,
      String locationCode,
      String clientNumber,
      String userId
  ) {
    return
        entityTemplate
            .getDatabaseClient()
            .sql(ADD_CONTACT_ASSOCIATION)
            .bind("client_number", clientNumber)
            .bind("entity_id", entityId)
            .bind("location_code", locationCode)
            .bind("user_id", userId)
            .fetch()
            .one()
            .then();
  }

  private Mono<Void> processAdd(
      String clientNumber,
      String userId,
      List<ContactAssociationDto> entries
  ) {
    return Flux
        .fromIterable(entries)
        .filter(entry -> entry.operation().equals("add"))
        //If no contact association exists for the entityId and newLocationCode, then add it
        .filterWhen(entry -> hasContactAssociation(
                clientNumber,
                entry.entityId(),
                entry.newLocationCode()
            )
            .map(result -> !result) // Only add if it does not exist
        )
        .concatMap(entry -> applyAdd(
                entry.entityId(),
                entry.newLocationCode(),
                clientNumber,
                userId
            )
        )
        .then();
  }

  private Mono<Void> applyReplace(
      Long entityId,
      String oldLocationCode,
      String newLocationCode,
      String clientNumber,
      String userId
  ) {
    return
        entityTemplate
            .getDatabaseClient()
            .sql(REPLACE_CONTACT_ASSOCIATION)
            .bind("client_number", clientNumber)
            .bind("entity_id", entityId)
            .bind("old_location_code", oldLocationCode)
            .bind("new_location_code", newLocationCode)
            .bind("user_id", userId)
            .fetch()
            .one()
            .then();
  }

  private Mono<Void> processReplace(
      String clientNumber,
      String userId,
      List<ContactAssociationDto> entries
  ) {
    return Flux
        .fromIterable(entries)
        .filter(entry -> entry.operation().equals("replace"))
        //If no contact association exists for the entityId and newLocationCode, then add it
        .filterWhen(entry -> hasContactAssociation(
                clientNumber,
                entry.entityId(),
                entry.newLocationCode()
            )
                .map(result -> !result) // Only add if it does not exist
        )
        .concatMap(entry -> applyReplace(
                entry.entityId(),
                entry.oldLocationCode(),
                entry.newLocationCode(),
                clientNumber,
                userId
            )
        )
        .then();
  }

  private Mono<Boolean> hasContactAssociation(
      String clientNumber,
      Long entityId,
      String locationCode
  ) {
    return entityTemplate
        .getDatabaseClient()
        .sql(CHECK_CONTACT_EXIST)
        .bind("client_number", clientNumber)
        .bind("entity_id", entityId)
        .bind("location_code", locationCode)
        .fetch()
        .one()
        .map(result -> result.get("cnt"))
        .map(String::valueOf)
        .map(Long::parseLong)
        .map(value -> value > 0);
  }


}
