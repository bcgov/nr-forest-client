package ca.bc.gov.app.validator.patch;

import static java.util.function.Predicate.not;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.exception.ValidationException;
import ca.bc.gov.app.service.client.ClientLegacyService;
import ca.bc.gov.app.validator.PatchValidator;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class RegistrationNumberPatchValidator implements PatchValidator {

  private final ClientLegacyService legacyService;

  public static final List<String> COMPANY_PARAMS = List.of(
      "/client/registryCompanyTypeCode",
      "/client/corpRegnNmbr"
  );

  @Override
  public Predicate<JsonNode> shouldValidate() {
    return node -> node.has("path")
                   &&
                   COMPANY_PARAMS
                       .contains(node.get("path").asText());
  }

  @Override
  public Function<JsonNode, Mono<JsonNode>> validate(String clientNumber) {
    // No specific validation here, it will be handled by the global validator
    return Mono::just;
  }

  @Override
  public Function<JsonNode, Mono<JsonNode>> globalValidator(
      JsonNode globalForestClient,
      String clientNumber
  ) {
    return node -> {

      // We look for the specific company parameters in the global forest client
      Map<String, List<String>> values =
          StreamSupport
              .stream(
                  globalForestClient.spliterator(),
                  false
              )
              // By filtering entries with path only (i.e., all of them)
              .filter(jsonNode -> jsonNode.has("path"))
              // And then filtering for the specific company parameters
              .filter(jsonNode -> COMPANY_PARAMS.contains(jsonNode.get("path").asText()))
              // We map the path and value to a Pair
              .map(jsonNode -> Pair.of(jsonNode.get("path").asText(),
                  jsonNode.get("value").asText()))
              // Then we map it, where the key is the path without "/client/"
              // and the value is a List containing the value
              .map(pair -> Pair.of(pair.getLeft().replace("/client/", StringUtils.EMPTY),
                  List.of(pair.getRight())))
              // Finally, we collect them into a Map
              .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

      // If there are no values, we return the node as is
      if (values.isEmpty()) {
        return Mono.just(node);
      }

      log.info("Searching for entry with value {}{} for client number: {}",
          values.get("registryCompanyTypeCode"),
          values.get("corpRegnNmbr"),
          clientNumber
      );

      return
          legacyService
              .searchGeneric(
                  "corporationValues/" + clientNumber,
                  values
              )
              .collectList()
              .filter(not(List::isEmpty))
              .flatMap(entries ->
                  Mono.error(new ValidationException(List.of(getError(entries))))
              )
              .cast(JsonNode.class)
              .defaultIfEmpty(node);


    };
  }

  private static ValidationError getError(List<ForestClientDto> entries) {
    return new ValidationError("/client/registrationNumber",
        "Registration number is already associated to another client",
        extractClientNumber(entries)
    );
  }

  private static String extractClientNumber(List<ForestClientDto> entries) {
    return entries
        .stream()
        .map(ForestClientDto::clientNumber)
        .collect(Collectors.joining(", "));
  }


}
