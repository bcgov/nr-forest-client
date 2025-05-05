package ca.bc.gov.app.service.client.validators;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.function.Function;
import java.util.function.Predicate;
import reactor.core.publisher.Mono;

public interface PatchValidator {

  Predicate<JsonNode> shouldValidate();
  Function<JsonNode, Mono<JsonNode>> validate();

}
