package ca.bc.gov.app.validator.patch;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.legacy.ForestClientDetailsDto;
import ca.bc.gov.app.dto.legacy.ForestClientInformationDto;
import ca.bc.gov.app.exception.ValidationException;
import ca.bc.gov.app.service.client.ClientLegacyService;
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
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientIdentificationPatchValidator implements PatchValidator {

  public static final String BIRTHDATE_PATH = "/client/birthdate";
  public static final String ID_PATH = "/client/clientIdentification";
  private final ClientLegacyService legacyService;

  // FSADT1-2040 - Allow editor to edit if empty
  private final List<String> onlyEmptyPaths = List.of(
      ID_PATH,
      BIRTHDATE_PATH
  );

  @Override
  public Predicate<JsonNode> shouldValidate() {
    return node ->
        node.has("path")
        && node.get("path").asText().equals("/roles")
        && node.get("roles").asText(StringUtils.EMPTY).contains(ApplicationConstant.ROLE_EDITOR);
  }

  @Override
  public Function<JsonNode, Mono<JsonNode>> validate(String clientNumber) {
    // No specific validation here, it will be handled by the global validator
    return Mono::just;
  }

  @Override
  public Function<JsonNode, Mono<JsonNode>> globalValidator(
      JsonNode globalForestClient, String clientNumber
  ) {

    List<String> paths =
        StreamSupport
            .stream(globalForestClient.spliterator(), false)
            .filter(jsonNode -> jsonNode.has("path"))
            .map(jsonNode -> jsonNode.get("path").asText())
            .filter(onlyEmptyPaths::contains)
            .toList();

    return node ->
        legacyService
            .searchByClientNumber(clientNumber)
            .map(ForestClientDetailsDto::client)
            .flatMap(processBirthdate(paths))
            .flatMap(processId(paths))
            .map(dto -> node)
            .defaultIfEmpty(node);
  }

  /**
   * Check if birthdate can be updated by editor role. In order to do that, birthdate must be null.
   * If not, throw validation exception.
   * @param paths list of paths being updated
   * @return Either the original dto or a Mono error if validation fails
   */
  private Function<ForestClientInformationDto, Mono<ForestClientInformationDto>> processBirthdate(
      List<String> paths
  ) {
    return dto ->
        Mono
            .just(dto)
            .filter(client ->
                (
                    client.birthdate() != null
                    && paths.contains(BIRTHDATE_PATH)
                )
            )
            .flatMap(path -> Mono.error(
                    new ValidationException(
                        List.of(new ValidationError(BIRTHDATE_PATH,
                            "Cannot update value. " + ApplicationConstant.ROLE_EDITOR
                            + " can only update value if is null", null))
                    )
                )
            )
            .cast(ForestClientInformationDto.class)
            .defaultIfEmpty(dto);
  }

  /**
   * Check if client identification can be updated by editor role. In order to do that,
   * client identification must be null or blank. If not, throw validation exception.
   * @param paths list of paths being updated
   * @return Either the original dto or a Mono error if validation fails
   */
  private Function<ForestClientInformationDto, Mono<ForestClientInformationDto>> processId(
      List<String> paths
  ) {
    return dto ->
        Mono
            .just(dto)
            .filter(client ->
                (
                    StringUtils.isNotBlank(client.clientIdentification())
                    && paths.contains(ID_PATH)
                )
            )
            .flatMap(path -> Mono.error(
                    new ValidationException(
                        List.of(new ValidationError(ID_PATH,
                            "Cannot update value. " + ApplicationConstant.ROLE_EDITOR
                            + " can only update value if is null", null))
                    )
                )
            )
            .cast(ForestClientInformationDto.class)
            .defaultIfEmpty(dto);
  }

}
