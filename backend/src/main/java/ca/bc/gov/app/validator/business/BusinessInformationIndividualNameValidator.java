package ca.bc.gov.app.validator.business;

import static ca.bc.gov.app.util.ClientValidationUtils.US7ASCII_PATTERN;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.BusinessTypeEnum;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientTypeEnum;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.validator.ForestClientValidator;
import io.micrometer.observation.annotation.Observed;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Observed
@Slf4j
@RequiredArgsConstructor
public class BusinessInformationIndividualNameValidator implements
    ForestClientValidator<ClientBusinessInformationDto> {

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return ValidationSourceEnum.STAFF.equals(source);
  }

  @Override
  public Mono<ValidationError> validate(ClientBusinessInformationDto target, Integer index) {

    if (
        BusinessTypeEnum.U.equals(
            BusinessTypeEnum.fromValue(target.businessType())
        )
            &&
            ClientTypeEnum.I.equals(
                ClientTypeEnum.fromValue(target.clientType())
            )
    ) {

      String fieldName = "businessInformation.";

      if (StringUtils.isBlank(target.firstName())) {
        return Mono.just(new ValidationError(fieldName+"firstName", "You must enter a first name"));
      }

      if (StringUtils.isBlank(target.lastName())) {
        return Mono.just(new ValidationError(fieldName+"lastName", "You must enter a last name"));
      }

      return
          validateName(fieldName + "firstName", target.firstName())
              .switchIfEmpty(validateName(fieldName + "lastName", target.lastName()))
              .switchIfEmpty(
                  Mono
                      .justOrEmpty(Optional.ofNullable(target.middleName()))
                      .filter(StringUtils::isNotBlank)
                      .flatMap(middleName -> validateName(fieldName + "middleName", middleName))
              );
    }

    return Mono.empty();
  }

  private Mono<ValidationError> validateName(String fieldName, String fieldValue) {
    if (!US7ASCII_PATTERN.matcher(fieldValue).matches()) {
      return Mono.just(new ValidationError(fieldName,
          String.format("%s has an invalid character.", fieldValue)));
    }

    if (StringUtils.length(fieldValue) > 30) {
      return Mono.just(
          new ValidationError(fieldName, "This field has a 30 character limit."));
    }
    return Mono.empty();
  }
}
