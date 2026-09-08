package ca.bc.gov.app.validator;

import java.util.function.Function;
import java.util.function.Predicate;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;

public interface PatchValidator {

  Predicate<JsonNode> shouldValidate();
  
  Function<JsonNode, Mono<JsonNode>> validate(String clientNumber);
  
  default Function<JsonNode,Mono<JsonNode>> globalValidator(JsonNode globalForestClient, String clientNumber) {
    return Mono::just;
  }
  
}
