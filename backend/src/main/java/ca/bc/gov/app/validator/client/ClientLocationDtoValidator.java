package ca.bc.gov.app.validator.client;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class ClientLocationDtoValidator implements Validator {
  private final ClientAddressDtoValidator addressDtoValidator;
  private final ClientContactDtoValidator contactDtoValidator;

  @Override
  public boolean supports(Class<?> clazz) {
    return ClientLocationDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    ClientLocationDto location = (ClientLocationDto) target;

    validateAddresses(errors, location);
    validateContacts(errors, location);


  }

  private void validateAddresses(Errors errors, ClientLocationDto location) {
    if (CollectionUtils.isEmpty(location.addresses())) {
      errors.pushNestedPath("location");
      errors.rejectValue("addresses", "addresses are missing");
      errors.popNestedPath();
      return;
    }

    List<ClientAddressDto> addresses = location.addresses();

    for (int i = 0; i < addresses.size(); ++i) {
      try {
        errors.pushNestedPath("location.addresses[" + i + "]");
        ValidationUtils
            .invokeValidator(addressDtoValidator, addresses.get(i), errors);
      } finally {
        errors.popNestedPath();
      }
    }
  }

  private void validateContacts(Errors errors, ClientLocationDto location) {
    if (CollectionUtils.isEmpty(location.contacts())) {
      errors.pushNestedPath("location");
      errors.rejectValue("contacts", "contacts are missing");
      errors.popNestedPath();
      return;
    }

    List<ClientContactDto> contacts = location.contacts();

    for (int i = 0; i < contacts.size(); ++i) {
      try {
        errors.pushNestedPath("location.contacts[" + i + "]");
        ValidationUtils
            .invokeValidator(contactDtoValidator, contacts.get(i), errors);
      } finally {
        errors.popNestedPath();
      }
    }
  }

}
