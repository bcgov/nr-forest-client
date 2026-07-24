package ca.bc.gov.app.validator.business;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.client.LegalTypeEnum;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.service.client.ClientLegacyService;
import ca.bc.gov.app.validator.ForestClientValidator;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Observed
@Slf4j
@RequiredArgsConstructor
public class BusinessInformationClientTypeCompanyXrefValidator implements
    ForestClientValidator<ClientBusinessInformationDto> {

  private final ClientLegacyService clientLegacyService;

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return true;
  }

  @Override
  public Mono<ValidationError> validate(ClientBusinessInformationDto target, Integer index) {

    if (StringUtils.isBlank(target.clientType())) {
      return Mono.empty();
    }

    LegalTypeEnum legalType = LegalTypeEnum.fromValue(target.legalType());
    if (legalType == null) {
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
                "businessInformation.legalType",
                String.format(
                    "Legal type %s is not valid for client type %s",
                    target.legalType(),
                    target.clientType()
                )
            )
        )
        .filter(ValidationError::isValid);
  }

}
