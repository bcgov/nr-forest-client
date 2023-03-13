package ca.bc.gov.app.validator.client;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class ClientAddressDtoValidator implements Validator {
  private ClientContactDtoValidator contactDtoValidator;

  @Override
  public boolean supports(Class<?> clazz) {
    return ClientAddressDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    ValidationUtils.rejectIfEmpty(
        errors, "contacts", "contacts.empty");

    ClientAddressDto address = (ClientAddressDto) target;

    if(StringUtils.isBlank(address.streetAddress())) {
      errors.rejectValue("streetAddress", "streetAddress.empty");
    }

    if(StringUtils.isBlank(address.country())) {
      errors.rejectValue("country", "country.empty");
    }

    if(StringUtils.isBlank(address.streetAddress())) {
      errors.rejectValue("province", "province.empty");
    }

    if(StringUtils.isBlank(address.streetAddress())) {
      errors.rejectValue("city", "city.empty");
    }

    if(StringUtils.isBlank(address.postalCode())) {
      errors.rejectValue("postalCode", "postalCode.empty");
    }

    if(CollectionUtils.isEmpty(address.contacts())) {
      errors.rejectValue("contacts", "contacts.empty");
    }

    for (ClientContactDto contact : address.contacts()) {
      ValidationUtils
          .invokeValidator(contactDtoValidator, contact, errors);
    }
  }
}
