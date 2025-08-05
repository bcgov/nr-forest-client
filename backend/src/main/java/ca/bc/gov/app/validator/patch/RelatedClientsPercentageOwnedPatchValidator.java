package ca.bc.gov.app.validator.patch;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.exception.ValidationException;
import ca.bc.gov.app.validator.PatchValidator;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RelatedClientsPercentageOwnedPatchValidator implements PatchValidator {

  private static final Pattern PERCENTAGE_OWNED_TYPE_PATH_PATTERN =
      Pattern.compile("^/relatedClients/(\\d{2})/(\\d+)/percentageOwnership$");

  @Override
  public Predicate<JsonNode> shouldValidate() {
    return node -> {
      String op = node.path("op").asText();
      String path = node.path("path").asText();
      return "replace".equals(op) && PERCENTAGE_OWNED_TYPE_PATH_PATTERN.matcher(path).matches();
    };
  }

  @Override
  public Function<JsonNode, Mono<JsonNode>> validate(String clientNumber) {
    return (node) -> {
      String path = node.path("path").asText();
      JsonNode valueNode = node.get("value");

      if (valueNode != null && !valueNode.isMissingNode() && !valueNode.isNull()) {

        if (valueNode.isNumber()) {
          double value = valueNode.asDouble();

          if (value < 0 || value > 100) {
            return Mono.error(new ValidationException(
                List.of(new ValidationError(path,
                    "The percentage owned must be between 0 and 100", null))));
          }
        } else {
          return Mono.error(new ValidationException(
              List.of(new ValidationError(path,
                  "The percentage owned must be a number between 0 and 100", null))));
        }

      }

      return Mono.just(node);
    };
  }

}
