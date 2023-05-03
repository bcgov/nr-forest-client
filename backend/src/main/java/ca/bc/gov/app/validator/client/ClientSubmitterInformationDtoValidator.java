package ca.bc.gov.app.validator.client;

import static ca.bc.gov.app.util.ClientValidationUtils.fieldIsMissingErrorMessage;
import static ca.bc.gov.app.util.ClientValidationUtils.validateEmail;
import static ca.bc.gov.app.util.ClientValidationUtils.validatePhoneNumber;

import ca.bc.gov.app.dto.client.ClientSubmitterInformationDto;
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

    String userId = "userId";
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, userId,
        fieldIsMissingErrorMessage(userId));


    String submitterFirstName = "submitterFirstName";
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, submitterFirstName,
        fieldIsMissingErrorMessage(submitterFirstName));

    String submitterLastName = "submitterLastName";
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, submitterLastName,
        fieldIsMissingErrorMessage(submitterLastName));

    ClientSubmitterInformationDto submitterInformation = (ClientSubmitterInformationDto) target;

    validatePhoneNumber(
        submitterInformation.submitterPhoneNumber(), "submitterPhoneNumber", errors);

    validateEmail(submitterInformation.submitterEmail(), "submitterEmail", errors);

    errors.popNestedPath();
  }
}
