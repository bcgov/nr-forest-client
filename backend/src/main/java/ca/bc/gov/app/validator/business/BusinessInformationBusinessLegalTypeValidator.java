package ca.bc.gov.app.validator.business;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.BusinessTypeEnum;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientTypeEnum;
import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.client.LegalTypeEnum;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.service.client.ClientLegacyService;
import ca.bc.gov.app.util.ClientValidationUtils;
import ca.bc.gov.app.validator.ForestClientValidator;
import io.micrometer.observation.annotation.Observed;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Observed
@Slf4j
@RequiredArgsConstructor
public class BusinessInformationBusinessLegalTypeValidator implements
    ForestClientValidator<ClientBusinessInformationDto> {

  private final ClientLegacyService clientLegacyService;

  private static final Set<String> VIRTUAL_CLIENT_TYPES = Set.of("RSP", "USP");

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return true;
  }

  @Override
  public Mono<ValidationError> validate(ClientBusinessInformationDto target, Integer index) {
    String fieldName = "businessInformation.legalType";

    return  validateLegalType(target, fieldName)
        .switchIfEmpty(validateClientToLegalMatch(target, fieldName))
        .switchIfEmpty(validateAgainstXref(target, fieldName));
  }

  private Mono<ValidationError> validateLegalType(
      ClientBusinessInformationDto target,
      String fieldName
  ) {
    return
        Mono
            .justOrEmpty(
                Optional
                    .ofNullable(target.legalType())
                    .map(LegalTypeEnum::fromValue)
                    .filter(Objects::nonNull)
                    .map(value -> new ValidationError("", ""))
                    .orElse(new ValidationError(
                            fieldName,
                            String.format("Legal type has an invalid value [%s]", target.legalType())
                        )
                    )
            )
            .filter(ValidationError::isValid);
  }

  private Mono<ValidationError> validateClientToLegalMatch(
      ClientBusinessInformationDto target,
      String fieldName
  ) {

    if (!BusinessTypeEnum.R.equals(BusinessTypeEnum.fromValue(target.businessType()))) {
      return Mono.empty();
    }

    return Mono
        .justOrEmpty(
            Optional
                .ofNullable(target.legalType())
                .map(LegalTypeEnum::fromValue)
                .map(ClientValidationUtils::getClientType)
                .filter(Objects::nonNull)
                .filter(legalEnum -> legalEnum.equals(ClientTypeEnum.fromValue(target.clientType())))
                .map(value -> new ValidationError("", ""))
                .orElse(
                    new ValidationError(
                        fieldName,
                        String.format(
                            "%s value for clientType does not match the expected value for legal type",
                            target.clientType())
                    )
                )
        )
        .filter(ValidationError::isValid);
  }

  private Mono<ValidationError> validateAgainstXref(
      ClientBusinessInformationDto target,
      String fieldName
  ) {

    if (StringUtils.isBlank(target.clientType())
        || VIRTUAL_CLIENT_TYPES.contains(target.clientType())
        || LegalTypeEnum.fromValue(target.legalType()) == null) {
      return Mono.empty();
    }

    return clientLegacyService
        .findActiveRegistryTypeCodesByClientTypeCode(target.clientType())
        .map(CodeNameDto::code)
        .filter(code -> code.equals(target.legalType()))
        .hasElements()
        .filter(hasMatch -> hasMatch)
        .map(match -> new ValidationError("", ""))
        .defaultIfEmpty(
            new ValidationError(
                fieldName,
                String.format(
                    "Legal type %s is not valid for client type %s",
                    target.legalType(),
                    target.clientType()
                )
            )
        )
        .filter(ValidationError::isValid)
        .onErrorResume(e -> {
          log.warn("Unable to validate legal type against xref table: {}", e.getMessage());
          return Mono.empty();
        });
  }

}
