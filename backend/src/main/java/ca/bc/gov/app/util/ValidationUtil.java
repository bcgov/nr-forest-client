package ca.bc.gov.app.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.validation.Errors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationUtil {
  private static final EmailValidator emailValidator = EmailValidator.getInstance();

  public static void validateEmail(String email, String field, Errors errors) {
    if (StringUtils.isBlank(email)) {
      errors.rejectValue(field, fieldIsMissingErrorMessage(field));
      return;
    }

    if (!emailValidator.isValid(email)) {
      errors.rejectValue(field, "Invalid format");
    }
  }

  public static void validatePhoneNumber(String phoneNumber, String field, Errors errors) {
    if (StringUtils.isBlank(phoneNumber)) {
      errors.rejectValue(field, fieldIsMissingErrorMessage(field));
      return;
    }

    if (!StringUtils.isNumeric(phoneNumber)) {
      errors.rejectValue(field, "Invalid format");
    }

    if (StringUtils.length(phoneNumber) > 10) {
      errors.rejectValue(field, "Has more than 10 digits");
    }
  }

  public static String fieldIsMissingErrorMessage(String fieldName) {
    return String.format("%s is missing", fieldName);
  }
}
