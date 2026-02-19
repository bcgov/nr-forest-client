package ca.bc.gov.app.validator.patch;

import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.legacy.ForestClientDetailsDto;
import ca.bc.gov.app.dto.legacy.ForestClientLocationDetailsDto;
import ca.bc.gov.app.service.client.ClientLegacyService;
import ca.bc.gov.app.util.PatchUtils;
import ca.bc.gov.app.validator.PatchValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientStatusPatchValidator implements PatchValidator {

  private final ClientLegacyService legacyService;
  private final ObjectMapper mapper;

  @Override
  public Predicate<JsonNode> shouldValidate() {
    return node ->
        node.has("path")
        && node.get("path").asText().endsWith("/clientStatusCode")
        && node.get("op").asText().equals("replace")
        && (
            node.get("value").asText().equals("DAC")
            || node.get("value").asText().equals("ACT")
        );
  }

  @Override
  public Function<JsonNode, Mono<JsonNode>> validate(String clientNumber) {
    return Mono::just;
  }

  @Override
  public Function<JsonNode, Mono<JsonNode>> globalValidator(JsonNode globalForestClient,
      String clientNumber) {
    return dto -> {
      boolean deactivate = dto.get("value").asText().equals("DAC");

      Flux<String> locationStream = deactivate
          ? getActiveLocations(clientNumber)
          : getDeactivatedLocations(clientNumber);

      return locationStream
          .doOnNext(location -> log.info("Updating client {} location {} to {}",
              clientNumber, location, deactivate ? "deactivated" : "activated")
          )
          .map(location -> updateLocation(location, deactivate))
          .reduce(dto, PatchUtils.mergeNodes(mapper));
    };
  }

  private @NonNull Flux<String> getActiveLocations(String clientNumber) {
    return legacyService
        .searchByClientNumber(clientNumber)
        .filter(client -> client.addresses() != null)
        .flatMapIterable(ForestClientDetailsDto::addresses)
        .filter(filterByExpired())
        .map(ForestClientLocationDetailsDto::clientLocnCode);
  }

  private @NonNull Flux<String> getDeactivatedLocations(String clientNumber) {
    return legacyService
        .findAllLocationUpdatedWithClient(clientNumber, "DAC")
        .map(CodeNameDto::code);
  }

  JsonNode updateLocation(String locationId, boolean deactivate) {
    return mapper.createObjectNode()
        .put("op", "replace")
        .put("path", String.format("/addresses/%s/locnExpiredInd", locationId))
        .put("value", BooleanUtils.toString(deactivate, "Y", "N"));
  }

  private static @NonNull Predicate<ForestClientLocationDetailsDto> filterByExpired() {
    return location ->
        location
            .locnExpiredInd()
            .equalsIgnoreCase("n");
  }

}
