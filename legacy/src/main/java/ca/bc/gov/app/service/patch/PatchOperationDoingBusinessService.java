package ca.bc.gov.app.service.patch;

import ca.bc.gov.app.dto.ClientDoingBusinessAsDto;
import ca.bc.gov.app.entity.ClientDoingBusinessAsEntity;
import ca.bc.gov.app.repository.ClientDoingBusinessAsRepository;
import ca.bc.gov.app.service.ClientDoingBusinessAsService;
import ca.bc.gov.app.util.PatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@Observed
@RequiredArgsConstructor
@Order(11)
public class PatchOperationDoingBusinessService implements ClientPatchOperation {

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

      return dbaRepository
          .findByClientNumber(clientNumber)
          .doOnNext(dba -> log.info("Found DBAs for client {} as {}", clientNumber,
              dba.getDoingBusinessAsName()))
          .map(dba ->
              dba
                  .withDoingBusinessAsName(getDoingBusinessAsName(filteredNode))
                  .withUpdatedBy(userId)
                  .withUpdatedByUnit(70L) // We use 70 as the default org unit
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
                          getDoingBusinessAsName(filteredNode),
                          userId,
                          userId,
                          70L //We use 70 as the default org unit
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

  private String getDoingBusinessAsName(JsonNode patch) {
    if(patch.has(0) && patch.get(0).has("value")) {
      return patch.get(0).get("value").asText().toUpperCase(Locale.ROOT);
    }
    throw new IllegalArgumentException("No value provided for doing business as");
  }
}
