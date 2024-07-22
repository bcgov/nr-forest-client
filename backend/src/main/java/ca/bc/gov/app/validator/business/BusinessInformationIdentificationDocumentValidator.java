package ca.bc.gov.app.validator.business;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.BusinessTypeEnum;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientTypeEnum;
import ca.bc.gov.app.dto.client.IdentificationTypeEnum;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.validator.ForestClientValidator;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Observed
@Slf4j
public class BusinessInformationIdentificationDocumentValidator implements
    ForestClientValidator<ClientBusinessInformationDto> {

  @Override
  public boolean supports(ValidationSourceEnum source) {
    return ValidationSourceEnum.STAFF.equals(source);
  }

  @Override
  public Mono<ValidationError> validate(ClientBusinessInformationDto target, Integer index) {

    String typeField = "businessInformation.identificationType";
    String valueField = "businessInformation.clientIdentification";

    if (
        BusinessTypeEnum.U.equals(
            BusinessTypeEnum.fromValue(target.businessType())
        )
            &&
            ClientTypeEnum.I.equals(
                ClientTypeEnum.fromValue(target.clientType())
            )
    ) {

      if (StringUtils.isBlank(target.idType())) {
        return Mono.just(new ValidationError(typeField, "You must select an identification type"));
      }
      if (StringUtils.isBlank(target.clientIdentification())) {
        return Mono.just(
            new ValidationError(valueField, "You must enter an identification number"));
      }

      return switch (target.idType()) {
        case "BRTH" -> validateBirthCertificate(valueField, target.clientIdentification());
        case "BCDL" -> validateBCDL(valueField, target.clientIdentification());
        case "PASS" -> validatePassport(valueField, target.clientIdentification());
        case "CITZ" -> validateCanadianCitizenship(valueField, target.clientIdentification());
        case "FNID" -> validateFirstNationsId(valueField, target.clientIdentification());
        case "OTHR" -> validateOtherDocuments(valueField, target.clientIdentification());
        default -> validateDL(valueField, target.clientIdentification());
      };

    }

    return Mono.empty();
  }

  private Mono<ValidationError> validateBCDL(String fieldName, String value) {
    // driver's license should be between 7 and 8 numbers only
    if (!StringUtils.isNumeric(value) || (StringUtils.length(value) < 7
        || StringUtils.length(value) > 8)) {
      return Mono.just(
          new ValidationError(fieldName, "The driver's license must be a 7 or 8-digit number"));
    }
    return Mono.empty();
  }

  private Mono<ValidationError> validateDL(String fieldName, String value) {
    if (!StringUtils.isAlphanumeric(value) || (StringUtils.length(value) < 7
        || StringUtils.length(value) > 20)) {
      return Mono.just(new ValidationError(fieldName,
          "The driver's license must be between 7 and 20 characters and contain only letters and numbers"));
    }
    return Mono.empty();
  }

  private Mono<ValidationError> validateBirthCertificate(String fieldName, String value) {
    if (!StringUtils.isNumeric(value) || (StringUtils.length(value) < 12
        || StringUtils.length(value) > 13)) {
      return Mono.just(
          new ValidationError(fieldName, "The birth certificate must be a 12 or 13-digit number"));
    }
    return Mono.empty();
  }

  private Mono<ValidationError> validatePassport(String fieldName, String value) {
    if (!StringUtils.isAlphanumeric(value) || (StringUtils.length(value) != 8)) {
      return Mono.just(new ValidationError(fieldName,
          "The passport must be 8 characters and contain only letters and numbers"));
    }
    return Mono.empty();
  }

  private Mono<ValidationError> validateFirstNationsId(String fieldName, String value) {
    if (!StringUtils.isNumeric(value) || (StringUtils.length(value) != 10)) {
      return Mono.just(
          new ValidationError(fieldName, "The First Nations ID must be a 10-digit number"));
    }
    return Mono.empty();
  }

  private Mono<ValidationError> validateCanadianCitizenship(String fieldName, String value) {
    if (!StringUtils.isNumeric(value) || (StringUtils.length(value) != 8)) {
      return Mono.just(
          new ValidationError(fieldName, "The Canadian Citizenship must be a 8-digit number"));
    }
    return Mono.empty();
  }

  private Mono<ValidationError> validateOtherDocuments(String fieldName, String value) {
    // other documents should be between 3 and 40 characters long, and match the regex pattern /^[^:]+:\s?[^\s:]+$/ where the left side should be composed of A-Z 0-9 characters only and the right side should have a minimum of 1 character size of 3
    if (StringUtils.length(value) < 3 || StringUtils.length(value) > 40) {
      return Mono.just(new ValidationError(fieldName,
          "The other document must be between 3 and 40 characters long"));
    }

    if (!value.matches("^(?=.{3,40}$)[^:]+:\\s?[A-Z0-9]{3,}$")) {
      return Mono.just(new ValidationError(fieldName,
          "Other identification must follow the pattern: [ID Type] : [ID Value] such as 'USA Passport : 12345'"));
    }

    return Mono.empty();
  }

}
