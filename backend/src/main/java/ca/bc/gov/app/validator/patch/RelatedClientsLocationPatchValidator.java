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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RelatedClientsLocationPatchValidator implements PatchValidator {

  private static final Pattern LOCATION_PATH_PATTERN =
      Pattern.compile("^/relatedClients/(\\d{2})/(\\d+)/client/location$");

  @Override
  public Predicate<JsonNode> shouldValidate() {
    return node -> {
      String op = node.path("op").asText();
      String path = node.path("path").asText();
      return "replace".equals(op) && LOCATION_PATH_PATTERN.matcher(path).matches();
    };
  }

  @Override
  public Function<JsonNode, Mono<JsonNode>> validate(String clientNumber) {
    return node -> {
      String path = node.get("path").asText();
      JsonNode valueNode = node.get("value");

      if (isMissing(valueNode)) {
        return error(path, "A location must be selected");
      }

      JsonNode codeNode = valueNode.get("code");
      JsonNode nameNode = valueNode.get("name");

      if (isBlank(codeNode) || isBlank(nameNode)) {
        return error(path, "A location must be selected");
      }

      return Mono.just(node);
    };
  }

  private boolean isMissing(JsonNode node) {
    return node == null || node.isNull() || node.isMissingNode();
  }

  private boolean isBlank(JsonNode node) {
    return node == null || StringUtils.isBlank(node.asText());
  }

  private Mono<JsonNode> error(String path, String message) {
    return Mono.error(
        new ValidationException(
            List.of(new ValidationError(path, message, null))
        )
    );
  }
  
}
