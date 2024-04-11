package ca.bc.gov.app.validator.client;

import static ca.bc.gov.app.util.ClientValidationUtils.fieldIsMissingErrorMessage;
import static ca.bc.gov.app.util.ClientValidationUtils.US7ASCII_PATTERN;

import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
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

    String fieldName = "businessName";
    String fieldValue = (String) errors.getFieldValue(fieldName);

    if (fieldValue == null || fieldValue.isEmpty()) {
      errors.rejectValue(fieldName, fieldIsMissingErrorMessage(fieldName));
      errors.popNestedPath();
      return;
    }

    // fails if businessName does not contain whitespace, Ex: forest1 should fail, but forest 1 should pass
    if (!fieldValue.matches(".*\\s+.*")) {
      errors.rejectValue(fieldName, "Business name must be composed of first and last name");
    }

    if (!US7ASCII_PATTERN.matcher(fieldValue).matches()) {
      errors.rejectValue(fieldName, String.format("%s has an invalid character.", fieldValue));
    }
    
    if (StringUtils.length(fieldValue) > 60) {
      errors.rejectValue(fieldName, "This field has a 60 character limit.");
    }

    errors.popNestedPath();
  }

}
