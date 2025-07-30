package ca.bc.gov.app.validator.patch;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.exception.ValidationException;
import ca.bc.gov.app.validator.PatchValidator;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

public class RelatedClientsRelatedClientLocationPatchValidator implements PatchValidator {

  private static final Pattern RELATED_CLIENT_LOCATION_PATH_PATTERN =
      Pattern.compile("^/relatedClients/(\\d{2})/(\\d+)/relatedClient/location$");

  @Override
  public Predicate<JsonNode> shouldValidate() {
    return node -> {
      String op = node.path("op").asText();
      String path = node.path("path").asText();
      return "replace".equals(op) && RELATED_CLIENT_LOCATION_PATH_PATTERN.matcher(path).matches();
    };
  }

  @Override
  public Function<JsonNode, Mono<JsonNode>> validate(String clientNumber) {
    return (node) -> {
      JsonNode valueNode = node.get("value");

      String path = node.get("path").asText();

      if (valueNode == null || valueNode.isNull() || valueNode.isMissingNode()) {
        return Mono.error(
            new ValidationException(
                List.of(new ValidationError(
                    path, "A related client must have a location", null)
                )
            )
        );
      }

      JsonNode codeNode = valueNode.get("code");
      JsonNode nameNode = valueNode.get("name");

      if (codeNode == null || StringUtils.isBlank(codeNode.asText())
          || nameNode == null || StringUtils.isBlank(nameNode.asText())) {
        return Mono.error(
            new ValidationException(
                List.of(new ValidationError(
                    path, "A related client must have a location", null)
                )
            )
        );
      }

      return Mono.just(node);
    };
  }

}
