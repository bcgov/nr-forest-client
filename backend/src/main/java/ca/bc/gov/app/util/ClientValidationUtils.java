package ca.bc.gov.app.util;

import ca.bc.gov.app.dto.client.ClientTypeEnum;
import ca.bc.gov.app.dto.client.LegalTypeEnum;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.validation.Errors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientValidationUtils {
	
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
      errors.rejectValue(field, field + " has an invalid format");
    }

    if (StringUtils.length(phoneNumber) != 10) {
      errors.rejectValue(field, field + " has to be 10 digits");
    }
  }

  public static String fieldIsMissingErrorMessage(String fieldName) {
    return String.format("%s is missing", fieldName);
  }

  public static ClientTypeEnum getClientType(LegalTypeEnum legalType) {
    switch (legalType) {
      case A:
      case B:
      case BC:
      case C:
      case CP:
      case EPR:
      case FOR:
      case LIC:
      case REG:
        return ClientTypeEnum.C;
      case S:
      case XS:
        return ClientTypeEnum.S;
      case XCP:
        return ClientTypeEnum.A;
      case SP:
        return ClientTypeEnum.I;
      case GP:
        return ClientTypeEnum.P;
      case LP:
      case XL:
      case XP:
        return ClientTypeEnum.L;
    }
    return null;
  }
}
