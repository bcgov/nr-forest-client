package ca.bc.gov.app.validator.client;

import static ca.bc.gov.app.util.ClientValidationUtils.fieldIsMissingErrorMessage;

import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class UnregisteredBusinessInformationValidator implements Validator {
  @Override
  public boolean supports(Class<?> clazz) {
    return ClientBusinessInformationDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    errors.pushNestedPath("businessInformation");

    String businessNameField = "businessName";
    ValidationUtils.rejectIfEmpty(errors, businessNameField,
        fieldIsMissingErrorMessage(businessNameField));

    // fails if businessName does not contain whitespace, Ex: forest1 should fail, but forest 1 should pass
    String businessName = (String) errors.getFieldValue(businessNameField);
    if (businessName != null && !businessName.matches(".*\\s+.*")) {
      errors.rejectValue(businessNameField, "Business name must be composed of first and last name");
    }
    errors.popNestedPath();
  }
}
