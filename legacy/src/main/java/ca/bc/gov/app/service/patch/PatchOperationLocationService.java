package ca.bc.gov.app.service.patch;

import ca.bc.gov.app.ApplicationConstants;
import ca.bc.gov.app.entity.ForestClientLocationEntity;
import ca.bc.gov.app.repository.ForestClientLocationRepository;
import ca.bc.gov.app.util.PatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

/**
 * Service responsible for applying JSON Patch operations to a
 * {@link ca.bc.gov.app.entity.ForestClientLocationEntity}.
 * <p>
 * This service handles patch operations targeting client location-related data. It ensures that
 * restricted fields (such as {@code /cliLocnComment} and {@code /emailAddress}) are the only ones
 * being modified.
 * </p>
 */
@Service
@Slf4j
@Observed
@RequiredArgsConstructor
@Order(3)
public class PatchOperationLocationService implements ClientPatchOperation {

  private final ForestClientLocationRepository locationRepository;
  private final R2dbcEntityOperations entityTemplate;
  private final Map<String, String> fieldToDataField = Map.of(
      "/clientLocnName", "client_locn_name",
      "/emailAddress", "email_address",
      "/faxNumber", "fax_number",
      "/cellPhone", "cell_phone",
      "/homePhone", "home_phone",
      "/businessPhone", "business_phone",
      "/cliLocnComment", "cli_locn_comment"
  );

  @Override
  public String getPrefix() {
    return "addresses";
  }

  @Override
  public List<String> getRestrictedPaths() {
    return List.of("/cliLocnComment", "/emailAddress", "/faxNumber", "/cellPhone", "/homePhone",
        "/businessPhone", "/clientLocnName");
  }

  /**
   * Applies a JSON Patch document to a client location entity.
   *
   * @param clientNumber The unique identifier of the client to be patched.
   * @param patch        The JSON Patch document describing the changes.
   * @param mapper       The {@link ObjectMapper} used to deserialize and apply the patch.
   * @return A {@link Mono} that completes when the patch is applied.
   */
  @Override
  public Mono<Void> applyPatch(String clientNumber, JsonNode patch, ObjectMapper mapper) {
    // If there's a patch operation targeting client location data we move ahead
    if (PatchUtils.checkOperation(patch, getPrefix(), mapper)) {
      return applyReplacePatch(clientNumber, patch, mapper);
    }

    return Mono.empty();
  }

  /**
   * Applies a replace patch operation to the client location data. This is only for data that don't
   * require a reason code.
   *
   * @param clientNumber The client number
   * @param patch        The patch operation json node
   * @param mapper       The {@link ObjectMapper} used to deserialize and apply the patch.
   * @return A {@link Mono} that completes when the patch is applied.
   */
  private Mono<Void> applyReplacePatch(String clientNumber, JsonNode patch, ObjectMapper mapper) {
    //We load just the replace operations
    JsonNode filteredNodeOps = PatchUtils.filterOperationsByOp(
        patch,
        "replace",
        getPrefix(),
        getRestrictedPaths(),
        mapper
    );

    return Flux
        //We will loop through it using a flux from the ids
        .fromIterable(PatchUtils.loadIds(patch))
        //For each location that was changed
        .flatMap(locationNumber ->
            //We look it up in the database
            findClientLocation(clientNumber, locationNumber)
                .flatMap(entity ->
                    Mono
                        //We load the patch operations for the current location
                        .just(
                            PatchUtils.filterById(filteredNodeOps, mapper).apply(locationNumber)
                        )
                        //We use filterPatchOperation to remove the location number prefix
                        .map(node -> PatchUtils.filterPatchOperations(
                                node,
                                locationNumber,
                                getRestrictedPaths(),
                                mapper
                            )
                        )
                        //We convert the patch operations to a map to be used in an update op
                        .map(node ->
                            StreamSupport
                                .stream(node.spliterator(), false)
                                .filter(entry -> fieldToDataField.containsKey(
                                    entry.get("path").asText()))
                                .map(entry -> Pair.of(SqlIdentifier.unquoted(
                                        fieldToDataField.get(entry.get("path").asText())),
                                    (Object) entry.get("value").asText())
                                )
                                .collect(Collectors.toMap(Pair::getKey, Pair::getValue))
                        )
                        .filter(updateMapper -> !updateMapper.isEmpty())
                        .map(Update::from)
                        //We apply the patch to the entity and save it
                        .flatMap(update -> entityTemplate
                            .update(
                                getLocationIdentification(clientNumber, locationNumber),
                                update,
                                ForestClientLocationEntity.class
                            )
                        )
                        .doOnNext(clientChangesApplied -> log.info(
                            "Applying Client Location changes on {} client", clientChangesApplied))
                )
        )
        .then();
  }

  private Mono<ForestClientLocationEntity> findClientLocation(String clientNumber,
      String locationNumber) {
    return entityTemplate
        .selectOne(
            getLocationIdentification(clientNumber, locationNumber),
            ForestClientLocationEntity.class
        );
  }

  private static Query getLocationIdentification(String clientNumber, String locationNumber) {
    return Query
        .query(
            Criteria
                .where("CLIENT_LOCN_CODE").is(locationNumber)
                .and(ApplicationConstants.CLIENT_NUMBER).is(clientNumber)
        );
  }

}
