package ca.bc.gov.app.validator.client;

import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.entity.client.ClientTypeCodeEntity;
import ca.bc.gov.app.entity.client.ContactTypeCodeEntity;
import ca.bc.gov.app.repository.client.ClientTypeCodeRepository;
import ca.bc.gov.app.repository.client.ContactTypeCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import static ca.bc.gov.app.validator.common.CommonValidator.fieldIsMissingErrorMessage;
import static ca.bc.gov.app.validator.common.CommonValidator.validateEmail;
import static ca.bc.gov.app.validator.common.CommonValidator.validatePhoneNumber;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class ClientContactDtoValidator implements Validator {

  private final ContactTypeCodeRepository typeCodeRepository;

  @Override
  public boolean supports(Class<?> clazz) {
    return ClientContactDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName",
        fieldIsMissingErrorMessage("firstName"));

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName",
        fieldIsMissingErrorMessage("lastName"));

    ClientContactDto contact = (ClientContactDto) target;

    validateContactType(contact, errors);

    validatePhoneNumber(
        contact.phoneNumber(), "phoneNumber", errors);

    validateEmail(contact.email(), "email", errors);
  }

  @SneakyThrows
  private void validateContactType(ClientContactDto contact, Errors errors) {
    if (contact.contactType() == null || StringUtils.isBlank(contact.contactType().value())) {
      errors.rejectValue("contactType", fieldIsMissingErrorMessage("contactType"));
      return;
    }

    ContactTypeCodeEntity contactTypeCode = typeCodeRepository
        .findById(contact.contactType().value()).toFuture().get();

    if (contactTypeCode == null) {
      errors.rejectValue("contactType", "Contact Type "+contact.contactType().text()+" is invalid");
    }

  }

}
