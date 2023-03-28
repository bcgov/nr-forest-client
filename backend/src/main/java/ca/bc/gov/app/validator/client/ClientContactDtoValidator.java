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

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "type",
        fieldIsMissingErrorMessage("type"));

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName",
        fieldIsMissingErrorMessage("firstName"));

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName",
        fieldIsMissingErrorMessage("lastName"));

    ClientContactDto contact = (ClientContactDto) target;

    validatePhoneNumber(
        contact.phoneNumber(), "phoneNumber", errors);

    validateEmail(contact.email(), "email", errors);
  }
}
