package ca.bc.gov.app.util;

import ca.bc.gov.app.dto.client.ClientTypeEnum;
import ca.bc.gov.app.dto.client.LegalTypeEnum;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.validation.Errors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientValidationUtils {

  private static final EmailValidator emailValidator = EmailValidator.getInstance();

  public static void validateEmail(String email, String field, Errors errors) {
    if (StringUtils.isBlank(email)) {
      errors.rejectValue(field, "You must enter an email address");
      return;
    }

    if (!emailValidator.isValid(email)) {
      errors.rejectValue(field, "You must enter an email address in a valid format. For example: name@example.com");
    }
  }

  public static void validatePhoneNumber(String phoneNumber, String field, Errors errors) {
    if (StringUtils.isBlank(phoneNumber)) {
      errors.rejectValue(field, "The phone number must be a 10-digit number");
      return;
    }

    if (!StringUtils.isNumeric(phoneNumber)) {
      errors.rejectValue(field, "The phone number must be a 10-digit number");
    }

    if (StringUtils.length(phoneNumber) != 10) {
      errors.rejectValue(field, "The phone number must be 10 digits");
    }
  }

  public static String fieldIsMissingErrorMessage(String fieldName) {
    return String.format("%s is missing", fieldName);
  }

  public static <T extends Enum<T>> boolean isValidEnum(
      String value, String field, Class<T> enumClass, Errors errors) {
    if (value == null) {
      errors.rejectValue(field, fieldIsMissingErrorMessage(field));
      return false;
    }

    if (!EnumUtils.isValidEnum(enumClass, value)) {
      errors.rejectValue(field, String.format("%s has an invalid value", field));
      return false;
    }
    return true;
  }

  public static ClientTypeEnum getClientType(LegalTypeEnum legalType) {
    if(legalType == null)
      return null;
    return switch (legalType) {
      case A, B, BC, C, CP, EPR, FOR, LIC, REG -> ClientTypeEnum.C;
      case S, XS -> ClientTypeEnum.S;
      case XCP -> ClientTypeEnum.A;
      case SP -> ClientTypeEnum.I;
      case GP -> ClientTypeEnum.P;
      case LP, XL, XP -> ClientTypeEnum.L;
    };
  }
}
