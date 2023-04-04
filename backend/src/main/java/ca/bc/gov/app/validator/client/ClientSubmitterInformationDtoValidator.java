package ca.bc.gov.app.validator.client;

import ca.bc.gov.app.dto.client.ClientSubmitterInformationDto;

import static ca.bc.gov.app.validator.common.CommonValidator.fieldIsMissingErrorMessage;
import static ca.bc.gov.app.validator.common.CommonValidator.validateEmail;
import static ca.bc.gov.app.validator.common.CommonValidator.validatePhoneNumber;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ClientSubmitterInformationDtoValidator implements Validator {
  @Override
  public boolean supports(Class<?> clazz) {
    return ClientSubmitterInformationDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    errors.pushNestedPath("submitterInformation");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "submitterFirstName",
        fieldIsMissingErrorMessage("submitterFirstName"));

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "submitterLastName",
        fieldIsMissingErrorMessage("submitterLastName"));

    ClientSubmitterInformationDto submitterInformation = (ClientSubmitterInformationDto) target;

    validatePhoneNumber(
        submitterInformation.submitterPhoneNumber(), "submitterPhoneNumber", errors);

    validateEmail(submitterInformation.submitterEmail(), "submitterEmail", errors);

    errors.popNestedPath();
  }
}
