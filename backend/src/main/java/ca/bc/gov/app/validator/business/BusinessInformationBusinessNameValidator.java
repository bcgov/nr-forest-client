package ca.bc.gov.app.validator.business;

import static ca.bc.gov.app.util.ClientValidationUtils.US7ASCII_PATTERN;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.BusinessTypeEnum;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
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
public class BusinessInformationBusinessNameValidator implements
    ForestClientValidator<ClientBusinessInformationDto> {

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return true;
  }

  @Override
  public Mono<ValidationError> validate(ClientBusinessInformationDto target, Integer index) {
    String fieldName = "businessInformation.businessName";

    if (StringUtils.isBlank(target.businessName())) {
      return Mono.just(new ValidationError(fieldName, getMessageType(target)));
    }

    if (!US7ASCII_PATTERN.matcher(target.businessName()).matches()) {
      return Mono.just(new ValidationError(fieldName,
          String.format("%s has an invalid character.", target.businessName())));
    }

    if (StringUtils.length(target.businessName()) > 60) {
      return Mono.just(new ValidationError(fieldName, "This field has a 60 character limit."));
    }

    if (
        BusinessTypeEnum.U.equals(
            BusinessTypeEnum.fromValue(target.businessType())
        ) && !target.businessName().matches(".*\\s+.*")
    ) {
      return Mono.just(
          new ValidationError(
              fieldName,
              "Business name must be composed of first and last name"
          )
      );
    }

    return Mono.empty();
  }

  private String getMessageType(ClientBusinessInformationDto target) {
    return
        BusinessTypeEnum.R.equals(
            BusinessTypeEnum.fromValue(target.businessType())
        ) ? "You must select your B.C. registered business name from the list."
            : "You must enter a business name.";
  }

}
