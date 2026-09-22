package ca.bc.gov.app.service.patch;

import static ca.bc.gov.app.ApplicationConstants.DEFAULT_PAGE_SIZE;

import ca.bc.gov.app.dto.ClientDoingBusinessAsDto;
import ca.bc.gov.app.entity.ClientDoingBusinessAsEntity;
import ca.bc.gov.app.repository.ClientDoingBusinessAsRepository;
import ca.bc.gov.app.service.ClientDoingBusinessAsService;
import ca.bc.gov.app.util.PatchUtils;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Patch operation that handles the client "doing business as" name.
 */
@Service
@Slf4j
@Observed
@RequiredArgsConstructor
@Order(11)
public class PatchOperationDoingBusinessService implements ClientPatchOperation {

  public static final String DEFAULT_USER_ID = "idir\\ottomated";

  private final ClientDoingBusinessAsRepository dbaRepository;
  private final ClientDoingBusinessAsService service;

  @Override
  public String getPrefix() {
    return "doingBusinessAs";
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
      JsonNode filteredNode = PatchUtils.filterPatchOperations(
          patch,
          getPrefix(),
          getRestrictedPaths(),
          mapper
      );

      if (filteredNode.isEmpty()) {
        return Mono.empty();
      }

      if (isDeleteOperation(filteredNode)) {
        return handleRemove(clientNumber);
      }

      String dbaName = getDoingBusinessAsName(filteredNode);
      String effectiveUserId = StringUtils.defaultIfBlank(userId, DEFAULT_USER_ID);

      return dbaRepository
          .findByClientNumber(clientNumber)
          .doOnNext(dba -> log.info("Found DBAs for client {} as {}", clientNumber,
              dba.getDoingBusinessAsName()))
          .map(dba ->
              dba
                  .withDoingBusinessAsName(dbaName)
                  .withUpdatedBy(effectiveUserId)
                  .withUpdatedByUnit(DEFAULT_PAGE_SIZE) // We use 70 as the default org unit
                  .withUpdatedAt(LocalDateTime.now())
                  .withRevision(dba.getRevision() + 1)
          )
          .next()
          .flatMap(dbaRepository::save)
          //This is just a trick to have same return type as the switchIfEmpty below
          .map(ClientDoingBusinessAsEntity::getDoingBusinessAsName)
          .switchIfEmpty(
              Mono
                  .just(
                      new ClientDoingBusinessAsDto(
                          clientNumber,
                          dbaName,
                          effectiveUserId,
                          effectiveUserId,
                          DEFAULT_PAGE_SIZE //We use 70 as the default org unit
                      )
                  )
                  .doOnNext(dba -> log.info("No DBAs found for client {}, creating one as {}",
                      dba.clientNumber(), dba.doingBusinessAsName()))
                  .flatMap(service::saveAndGetIndex)
          )
          .then();

    }

    return Mono.empty();
  }

  /**
   * Removes every "doing business as" name of the client.
   *
   * @param clientNumber the client number that owns the names
   * @return a {@link Mono} that completes once every name has been removed
   */
  private Mono<Void> handleRemove(String clientNumber) {
    log.info("Removing DBA for client {}", clientNumber);
    return dbaRepository
        .findByClientNumber(clientNumber)
        .flatMap(dba ->
            dbaRepository
                .delete(dba)
                .doOnSuccess(unused -> log.info(
                    "Deleted DBA {} for client {}",
                    dba.getDoingBusinessAsName(),
                    clientNumber
                ))
        )
        .then();
  }

  private boolean isDeleteOperation(JsonNode patch) {
    if (patch.isArray() && patch.has(0)) {
      JsonNode opNode = patch.get(0);
      String op = opNode.path("op").asText();
      if ("remove".equalsIgnoreCase(op)) {
        return true;
      }
      if (opNode.has("value")) {
        JsonNode valueNode = opNode.get("value");
        return valueNode.isNull() || StringUtils.isBlank(valueNode.asText());
      }
    }
    return false;
  }

  private String getDoingBusinessAsName(JsonNode patch) {
    if (patch.isArray() && patch.has(0) && patch.get(0).hasNonNull("value")) {
      String name = patch.get(0).get("value").asText().trim().toUpperCase(Locale.ROOT);
      if (StringUtils.isNotBlank(name)) {
        return name;
      }
    }
    log.error("Invalid patch format: expected an array with a non-blank 'value' field");
    throw new IllegalArgumentException("No value provided for doing business as");
  }
}
