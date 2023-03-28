package ca.bc.gov.app.validator.client;

import static ca.bc.gov.app.util.ValidationUtil.fieldIsMissingErrorMessage;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.entity.client.ProvinceCodeEntity;
import ca.bc.gov.app.repository.client.ProvinceCodeRepository;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ClientAddressDtoValidator implements Validator {
  private final ClientContactDtoValidator contactDtoValidator;

  private final ProvinceCodeRepository provinceCodeRepository;

  private static final Pattern CA_POSTAL_CODE_FORMAT =
      Pattern.compile("[A-Z][0-9][A-Z][0-9][A-Z][0-9]");

  @Override
  public boolean supports(Class<?> clazz) {
    return ClientAddressDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "streetAddress",
        fieldIsMissingErrorMessage("streetAddress"));

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "city",
        fieldIsMissingErrorMessage("city"));

    ClientAddressDto address = (ClientAddressDto) target;

    validateCountryProvinceAndPostalCode(address, errors);

    validateContacts(address.contacts(), errors);
  }

  @SneakyThrows
  private void validateCountryProvinceAndPostalCode(ClientAddressDto address, Errors errors) {
    if (StringUtils.isBlank(address.country().value())) {
      errors.rejectValue("country", fieldIsMissingErrorMessage("country"));
      return;
    }
    validatePostalCode(address, errors);

    //Province is only mandatory if the countries are US or CA
    if ("US".equalsIgnoreCase(address.country().value())
        || "CA".equalsIgnoreCase(address.country().value())) {
      if (StringUtils.isBlank(address.province().value())) {
        errors.rejectValue("province", fieldIsMissingErrorMessage("province"));
        return;
      }

      ProvinceCodeEntity provinceCodeEntity = provinceCodeRepository
          .findByProvinceCode(address.province().value()).toFuture().get();

      if (provinceCodeEntity == null) {
        errors.rejectValue("province", "province is invalid");
        return;
      }

      //Province and country are not matching
      if (!address.country().value().equalsIgnoreCase(provinceCodeEntity.getCountryCode())) {
        errors.rejectValue("province", "province doesn't belong to country");
      }
    }
  }

  private void validatePostalCode(ClientAddressDto address, Errors errors) {
    if (StringUtils.isBlank(address.postalCode())) {
      errors.rejectValue("postalCode", fieldIsMissingErrorMessage("postalCode"));
      return;
    }

    if ("CA".equalsIgnoreCase(address.country().value())) {
      //For CA, postal code should be up to 7 characters
      if (StringUtils.length(address.postalCode()) > 6) {
        errors.rejectValue("postalCode", "has more than 7 characters");
      }
      //CA postal code format is A9A9A9
      if (!CA_POSTAL_CODE_FORMAT.matcher(address.postalCode()).matches()) {
        errors.rejectValue("postalCode", "invalid Canada postal code format");
      }
    } else if ("US".equalsIgnoreCase(address.country().value())) {
      //For US, postal code should be digits (numbers only)
      if (!StringUtils.isNumeric(address.postalCode())) {
        errors.rejectValue("postalCode", "should be numeric");
      }
      //For US, postal code should be up to 5 digits
      if (StringUtils.length(address.postalCode()) > 5) {
        errors.rejectValue("postalCode", "has more than 5 characters");
      }
    } else {
      //For other countries postal code should be up to 10 characters
      if (StringUtils.length(address.postalCode()) > 10) {
        errors.rejectValue("postalCode", "has more than 10 characters");
      }
    }
  }

  private void validateContacts(List<ClientContactDto> contacts, Errors errors) {
    if (CollectionUtils.isEmpty(contacts)) {
      errors.rejectValue("contacts", fieldIsMissingErrorMessage("contacts"));
      return;
    }

    for (int i = 0; i < contacts.size(); ++i) {
      try {
        errors.pushNestedPath("contacts[" + i + "]");
        ValidationUtils
            .invokeValidator(contactDtoValidator, contacts.get(i), errors);
      } finally {
        errors.popNestedPath();
      }
    }
  }
}
