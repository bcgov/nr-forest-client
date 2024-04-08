package ca.bc.gov.app.validator.client;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.entity.client.CountryCodeEntity;
import ca.bc.gov.app.entity.client.ProvinceCodeEntity;
import ca.bc.gov.app.repository.client.CountryCodeRepository;
import ca.bc.gov.app.repository.client.ProvinceCodeRepository;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class ClientAddressDtoValidator implements Validator {

  private final ProvinceCodeRepository provinceCodeRepository;
  private final CountryCodeRepository countryCodeRepository;

  private static final Pattern CA_POSTAL_CODE_FORMAT = Pattern.compile("[A-Z]\\d[A-Z]\\d[A-Z]\\d");
  private static final Pattern US_ZIP_CODE_FORMAT = Pattern.compile("^\\d{5}(?:-\\d{4})?$");

  @Override
  public boolean supports(Class<?> clazz) {
    return ClientAddressDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "streetAddress",
        "You must enter a street address or PO box number");

    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "city",
        "You must enter the name of a city or town.");

    ClientAddressDto address = (ClientAddressDto) target;

    String country = validateCountry(address, errors);

    validatePostalCode(address, country, errors);

    validateProvince(address, country, errors);
  }

  @SneakyThrows
  private String validateCountry(ClientAddressDto address, Errors errors) {
    String country = address.country().value();
    String countryField = "country";
    
    if (StringUtils.isBlank(country)) {
      errors.rejectValue(countryField, "You must select a country from the list.");
      return StringUtils.EMPTY;
    }
    else {
      CountryCodeEntity countryCodeEntity = countryCodeRepository
          .findByCountryCode(country).toFuture().get();
      
      if (countryCodeEntity == null) {
        errors.rejectValue(countryField, "country is invalid");
        return StringUtils.EMPTY;
      }
    }

    return country;
  }

  @SneakyThrows
  private void validateProvince(ClientAddressDto address, String country, Errors errors) {

    String provinceField = "province";

    String province = address.province().value();

    if (StringUtils.isBlank(province)) {
      if ("CA".equalsIgnoreCase(country)) {
        errors.rejectValue(provinceField, "You must select a province or territory.");
        return;
      }
      else if ("US".equalsIgnoreCase(country)) {
        errors.rejectValue(provinceField, "You must select a state.");
        return;
      } 
      else {
        errors.rejectValue(provinceField, "You must select a province, territory or state.");
        return;
      }
    }

    // Validate that province is from the correct country, only if country is US or CA
    if ("US".equalsIgnoreCase(country) || "CA".equalsIgnoreCase(country)) {
      ProvinceCodeEntity provinceCodeEntity = provinceCodeRepository
          .findByCountryCodeAndProvinceCode(country, province).toFuture().get();

      if (provinceCodeEntity == null) {
        errors.rejectValue(provinceField, "province is invalid");
        return;
      }

      // Province and country are not matching
      if (!country.equalsIgnoreCase(provinceCodeEntity.getCountryCode())) {
        errors.rejectValue(provinceField, "province doesn't belong to country");
      }
    }
  }

  private void validatePostalCode(ClientAddressDto address, String country, Errors errors) {
    String postalCodeField = "postalCode";
    
    if (StringUtils.isBlank(country)) {
      return;
    }
    
    handleBlankPostalCode(address.postalCode(), country, postalCodeField, errors);

    handlePostalCodeLengthAndFormat(address.postalCode(), country, postalCodeField, errors);
  }

  private void handleBlankPostalCode(String postalCode, String country, String postalCodeField,
      Errors errors) {
    if (StringUtils.isBlank(postalCode)) {
      if ("CA".equalsIgnoreCase(country)) {
        errors.rejectValue(postalCodeField, "You must include a postal code in the format A9A9A9.");
      } 
      else if ("US".equalsIgnoreCase(country)) {
        errors.rejectValue(postalCodeField, "You must include a ZIP code in the format 000000 or 00000-0000.");
      } 
      else {
        errors.rejectValue(postalCodeField, "You must include a postal code.");
      }
    }
  }
  
  private void handlePostalCodeLengthAndFormat(String postalCode, String country,
      String postalCodeField, Errors errors) {
    if ("CA".equalsIgnoreCase(country)) {
      // For CA, postal code should be up to 6 characters
      if (StringUtils.length(postalCode) > 6) {
        errors.rejectValue(postalCodeField, "has more than 6 characters");
      }
      // CA postal code format is A9A9A9
      if (!CA_POSTAL_CODE_FORMAT.matcher(postalCode).matches()) {
        errors.rejectValue(postalCodeField, "invalid Canada postal code format");
      }
    } 
    else if ("US".equalsIgnoreCase(country)) {
      // For US, postal code should be up to 10 digits
      if (StringUtils.length(postalCode) > 10) {
        errors.rejectValue(postalCodeField, "has more than 10 characters");
      }
      // CA postal code format is A9A9A9
      if (!US_ZIP_CODE_FORMAT.matcher(postalCode).matches()) {
        errors.rejectValue(postalCodeField, "invalid US zip code format");
      }
    } 
    else {
      // For other countries postal code should be up to 10 characters
      if (StringUtils.length(postalCode) > 10) {
        errors.rejectValue(postalCodeField, "has more than 10 characters");
      }
    }
  }

}
