package ca.bc.gov.app.validator.patch;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.exception.ValidationException;
import ca.bc.gov.app.validator.PatchValidator;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminRolePatchValidator implements PatchValidator {

  private final List<String> adminOnlyPaths = List.of(
      "/client/clientName",
      "/client/legalMiddleName",
      "/client/legalFirstName",
      "/client/corpRegnNmbr",
      "/client/registryCompanyTypeCode",
      "/client/clientIdentification",
      "/client/clientIdTypeCode",
      "/doingBusinessAs",
      "/client/clientTypeCode"
  );

  @Override
  public Predicate<JsonNode> shouldValidate() {
    return node ->
        node.has("path")
        && node.get("path").asText().equals("/roles")
        && !node.get("roles").asText(StringUtils.EMPTY).contains(ApplicationConstant.ROLE_ADMIN);
  }

  @Override
  public Function<JsonNode, Mono<JsonNode>> validate(String clientNumber) {
    // No specific validation here, it will be handled by the global validator
    return Mono::just;
  }

  @Override
  public Function<JsonNode, Mono<JsonNode>> globalValidator(JsonNode globalForestClient,
      String clientNumber) {
    return node ->
        Flux
            .fromStream(StreamSupport.stream(
                    globalForestClient.spliterator(),
                    false
                )
            )
            .filter(jsonNode -> jsonNode.has("path"))
            .map(jsonNode -> jsonNode.get("path").asText())
            .filter(adminOnlyPaths::contains)
            .flatMap(path -> Mono.error(
                    new ValidationException(
                        List.of(new ValidationError(path,
                            "Cannot update value. User needs FAM Role " + ApplicationConstant.ROLE_ADMIN
                            + " in order to do so", null))
                    )
                )
            )
            .next()
            .cast(JsonNode.class)
            .defaultIfEmpty(node);
  }
}
