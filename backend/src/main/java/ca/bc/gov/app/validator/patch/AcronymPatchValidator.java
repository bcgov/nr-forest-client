package ca.bc.gov.app.validator.patch;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.exception.ValidationException;
import ca.bc.gov.app.service.client.ClientLegacyService;
import ca.bc.gov.app.validator.PatchValidator;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AcronymPatchValidator implements PatchValidator {

  private final ClientLegacyService legacyService;

  @Override
  public Predicate<JsonNode> shouldValidate() {
    return node ->
        node.has("path")
        && node.get("path").asText().endsWith("/clientAcronym")
        && node.get("op").asText().equals("replace");
  }

  @Override
  public Function<JsonNode, Mono<JsonNode>> validate(String clientNumber) {
    return (node) -> validateSize(node).flatMap(this::validateUniqueness);
  }

  private static ValidationException getError(String message, String clientNumber) {
    return new ValidationException(
        List.of(new ValidationError("/client/clientAcronym", message, clientNumber))
    );
  }

  private Mono<JsonNode> validateSize(JsonNode node) {
    String clientAcronym = node.get("value").asText();
    if (clientAcronym.length() > 8 || clientAcronym.length() < 3) {
      return Mono.error(getError("Client acronym must be between 3 and 8 characters", null));
    }
    return Mono.just(node);
  }

  private Mono<JsonNode> validateUniqueness(JsonNode node) {
    return legacyService
        .searchGeneric(
            "acronym",
            Map.of("acronym", List.of(node.get("value").asText()))
        )
        .next()
        .flatMap(
            value -> Mono.error(getError("Client acronym already exists", value.clientNumber())))
        .cast(JsonNode.class)
        .defaultIfEmpty(node);
  }


}
