package ca.bc.gov.app.service.patch;

import ca.bc.gov.app.ApplicationConstants;
import ca.bc.gov.app.dto.ForestClientLocationDetailsDto;
import ca.bc.gov.app.entity.ForestClientLocationEntity;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
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
public class PatchOperationLocationService implements
    ClientPatchUpdateOperation<ForestClientLocationEntity> {

  private final R2dbcEntityOperations entityTemplate;

  @Override
  public String getPrefix() {
    return "addresses";
  }

  @Override
  public List<String> getRestrictedPaths() {
    return List.of("/cliLocnComment", "/emailAddress", "/faxNumber", "/cellPhone", "/homePhone",
        "/businessPhone", "/clientLocnName");
  }

  @Override
  public Map<String, String> getFieldToDataField() {
    return Map.of(
        "/clientLocnName", "client_locn_name",
        "/emailAddress", "email_address",
        "/faxNumber", "fax_number",
        "/cellPhone", "cell_phone",
        "/homePhone", "home_phone",
        "/businessPhone", "business_phone",
        "/cliLocnComment", "cli_locn_comment"
    );
  }

  @Override
  public Function<ForestClientLocationEntity, Map<String, Object>> getExtraFields(String userId) {
    return entity -> Map.of(
        "update_timestamp", LocalDateTime.now(),
        "update_userid", userId,
        "update_org_unit", 70L,
        "revision_count", entity.getRevision() + 1
    );
  }

  @Override
  public Mono<ForestClientLocationEntity> findEntity(String clientNumber, String entityId) {
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
                .where("CLIENT_LOCN_CODE").is(entityId)
                .and(ApplicationConstants.CLIENT_NUMBER).is(clientNumber)
        );
  }

  @Override
  public Class<ForestClientLocationEntity> getEntityClass() {
    return ForestClientLocationEntity.class;
  }

  /**
   * Applies a JSON Patch document to a client location entity.
   *
   * @param clientNumber The unique identifier of the client to be patched.
   * @param patch        The JSON Patch document describing the changes.
   * @param mapper       The {@link ObjectMapper} used to deserialize and apply the patch.
   * @param userId       The username that requested the patch.
   * @return A {@link Mono} that completes when the patch is applied.
   */
  @Override
  public Mono<Void> applyPatch(String clientNumber, JsonNode patch, ObjectMapper mapper,
      String userId) {
    // If there's a patch operation targeting client location data we move ahead
    if (PatchUtils.checkOperation(patch, getPrefix(), mapper)) {
      return
          Flux.concat(
                  applyReplacePatch(clientNumber, patch, mapper, userId, entityTemplate),
                  applyAddPatch(clientNumber, patch, mapper, userId)
              )
              .then();
    }

    return Mono.empty();
  }

  private Mono<Void> applyAddPatch(String clientNumber, JsonNode patch, ObjectMapper mapper,
      String userId) {
    //We load just the add operations
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
            .map(entry -> PatchUtils.loadAddValue(entry, ForestClientLocationDetailsDto.class,
                mapper))
            .flatMap(dto -> getNextLocationCode(clientNumber).map(dto::withClientLocnCode))
            .doOnNext(System.out::println)
            .map(dto ->
                ForestClientLocationEntity
                    .builder()
                    .clientNumber(dto.clientNumber())
                    .clientLocnCode(dto.clientLocnCode())
                    .clientLocnName(uppercaseIt(dto.clientLocnName()))
                    .hdbsCompanyCode(StringUtils.SPACE)
                    .addressOne(uppercaseIt(dto.addressOne()))
                    .addressTwo(uppercaseIt(dto.addressTwo()))
                    .addressThree(uppercaseIt(dto.addressThree()))
                    .city(uppercaseIt(dto.city()))
                    .province(dto.provinceCode())
                    .postalCode(dto.postalCode())
                    .country(uppercaseIt(dto.countryDesc()))
                    .businessPhone(dto.businessPhone())
                    .homePhone(dto.homePhone())
                    .cellPhone(dto.cellPhone())
                    .faxNumber(dto.faxNumber())
                    .emailAddress(uppercaseIt(dto.emailAddress()))
                    .cliLocnComment(dto.cliLocnComment())
                    .locnExpiredInd("N")
                    .trustLocationInd("N")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .createdBy(userId)
                    .updatedBy(userId)
                    .createdByUnit(70L)
                    .updatedByUnit(70L)
                    .revision(1L)
                    .build()
            )
            .flatMap(entity -> entityTemplate
                .insert(ForestClientLocationEntity.class)
                .using(entity)
            )
            .doOnNext(clientChangesApplied -> log.info(
                "Adding Client Location on {} client", clientChangesApplied))
            .then();
  }

  private Mono<String> getNextLocationCode(String clientNumber) {
    return entityTemplate
        .getDatabaseClient()
        .sql(
            "SELECT MAX(CLIENT_LOCN_CODE) as locn_code FROM CLIENT_LOCATION WHERE CLIENT_NUMBER = :clientNumber")
        .bind("clientNumber", clientNumber)
        .map(row -> row.get("locn_code", String.class))
        .one()
        .map(code -> String.format("%02d", Integer.parseInt(code) + 1))
        .defaultIfEmpty("00");
  }

  private String uppercaseIt(String str) {
    return StringUtils.isNotBlank(str) ? str.toUpperCase() : str;
  }

}
