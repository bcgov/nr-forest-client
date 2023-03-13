package ca.bc.gov.app.validator.client;

import ca.bc.gov.app.dto.client.ClientContactDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ClientContactDtoValidator implements Validator {
  @Override
  public boolean supports(Class<?> clazz) {
    return ClientContactDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    ClientContactDto contact = (ClientContactDto) target;

    if(StringUtils.isBlank(contact.contactType())) {
      errors.rejectValue("contactType", "contactType.empty");
    }

    if(StringUtils.isBlank(contact.contactFirstName())) {
      errors.rejectValue("contactFirstName", "contactFirstName.empty");
    }

    if(StringUtils.isBlank(contact.contactLastName())) {
      errors.rejectValue("contactLastName", "contactLastName.empty");
    }

    if(StringUtils.isBlank(contact.contactPhoneNumber())) {
      errors.rejectValue("contactPhoneNumber", "contactPhoneNumber.empty");
    }

    //Todo: validate email format?
    if(StringUtils.isBlank(contact.contactEmail())) {
      errors.rejectValue("contactEmail", "contactEmail.empty");
    }
  }
}
