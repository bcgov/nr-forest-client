package ca.bc.gov.app.validator.client;

import static ca.bc.gov.app.util.ValidationUtil.fieldIsMissingErrorMessage;
import static ca.bc.gov.app.util.ValidationUtil.validateEmail;
import static ca.bc.gov.app.util.ValidationUtil.validatePhoneNumber;

import ca.bc.gov.app.dto.client.ClientContactDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ClientContactDtoValidator implements Validator {
  @Override
  public boolean supports(Class<?> clazz) {
    return ClientContactDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "contactType",
        fieldIsMissingErrorMessage("contactType"));

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "contactFirstName",
        fieldIsMissingErrorMessage("contactFirstName"));

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "contactLastName",
        fieldIsMissingErrorMessage("contactLastName"));

    ClientContactDto contact = (ClientContactDto) target;

    validatePhoneNumber(
        contact.contactPhoneNumber(), "contactPhoneNumber", errors);

    validateEmail(contact.contactEmail(), "contactEmail", errors);
  }
}
