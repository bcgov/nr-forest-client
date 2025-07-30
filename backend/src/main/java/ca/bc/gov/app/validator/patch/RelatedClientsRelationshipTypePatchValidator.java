package ca.bc.gov.app.validator.patch;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.exception.ValidationException;
import ca.bc.gov.app.validator.PatchValidator;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RelatedClientsRelationshipTypePatchValidator implements PatchValidator {
  
  private static final Pattern RELATIONSHIP_TYPE_PATH_PATTERN =
      Pattern.compile("^/relatedClients/(\\d{2})/(\\d+)/relationship$");

  @Override
  public Predicate<JsonNode> shouldValidate() {
    return node -> {
      String op = node.path("op").asText();
      String path = node.path("path").asText();
      return "replace".equals(op) && RELATIONSHIP_TYPE_PATH_PATTERN.matcher(path).matches();
    };
  }

  @Override
  public BiFunction<JsonNode, String, Mono<JsonNode>> validate() {
    return (node, clientNumber) -> {
      JsonNode valueNode = node.get("value");

      String path = node.get("path").asText();

      if (valueNode == null || valueNode.isNull() || valueNode.isMissingNode()) {
        return Mono.error(
            new ValidationException(
                List.of(new ValidationError(
                    path, "A related client must have a relationship type", null)
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
                    path, "A related client must have a relationship type", null)
                )
            )
        );
      }

      return Mono.just(node);
    };
  }
  
}
