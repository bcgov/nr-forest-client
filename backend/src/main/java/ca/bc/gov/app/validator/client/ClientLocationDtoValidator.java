package ca.bc.gov.app.validator.client;

import ca.bc.gov.app.dto.client.ClientAddressDto;
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

  @Override
  public boolean supports(Class<?> clazz) {
    return ClientLocationDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    ClientLocationDto location = (ClientLocationDto) target;

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
}
