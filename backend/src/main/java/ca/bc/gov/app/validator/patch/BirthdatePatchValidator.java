package ca.bc.gov.app.validator.patch;

import ca.bc.gov.app.validator.PatchValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BirthdatePatchValidator implements PatchValidator {

  @Override
  public Predicate<JsonNode> shouldValidate() {
    return node -> node.has("path")
                   && node.get("path").asText().endsWith("/birthdate");
  }

  @Override
  public Function<JsonNode, Mono<JsonNode>> validate(String clientNumber) {
    return (node) ->
        Mono
            .just(node)
            .map(aNode ->
                ((ObjectNode) node.deepCopy())
                    .put("value", aNode.get("value").asText() + " 00:00:00")
            );
  }
}
