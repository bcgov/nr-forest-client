package ca.bc.gov.app.validator.client;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class ClientLocationDtoValidator implements Validator {
  private ClientAddressDtoValidator addressDtoValidator;

  @Override
  public boolean supports(Class<?> clazz) {
    return ClientLocationDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    ValidationUtils.rejectIfEmpty(
        errors, "addresses", "addresses.empty");

    ClientLocationDto location = (ClientLocationDto) target;

    for (ClientAddressDto address : location.addresses()) {
      ValidationUtils
          .invokeValidator(addressDtoValidator, address, errors);
    }
  }
}
