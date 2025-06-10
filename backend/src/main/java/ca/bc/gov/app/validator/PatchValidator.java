package ca.bc.gov.app.validator;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.function.Function;
import java.util.function.Predicate;
import reactor.core.publisher.Mono;

public interface PatchValidator {

  Predicate<JsonNode> shouldValidate();
  Function<JsonNode, Mono<JsonNode>> validate();
  default Function<JsonNode,Mono<JsonNode>> globalValidator(JsonNode globalForestClient){
    return Mono::just;
  }
}
