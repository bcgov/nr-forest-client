package ca.bc.gov.app.util;

import ca.bc.gov.app.dto.client.ClientTypeEnum;
import ca.bc.gov.app.dto.client.LegalTypeEnum;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Errors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientValidationUtils {

  private static final String EMAIL_REGEX = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$";
  private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
  public static final Pattern US7ASCII_PATTERN = Pattern.compile("^[\\x00-\\x7F]+$");
  
  public static void validateEmail(String email, String field, Errors errors) {
    if (StringUtils.isBlank(email)) {
      errors.rejectValue(field, "You must enter an email address");
      return;
    }
    else if (StringUtils.length(email) > 100) {
      errors.rejectValue(field, "This field has a 100 character limit.");
      return;
    }

    Matcher emailMatcher = EMAIL_PATTERN.matcher(email);
    if (!emailMatcher.matches()) {
      errors.rejectValue(field, "You must enter an email address in a valid format. "
                                + "For example: name@example.com");
    }
  }

  public static void validatePhoneNumber(String phoneNumber, String field, Errors errors) {
    if (StringUtils.isBlank(phoneNumber)) {
      errors.rejectValue(field, "The phone number must be a 10-digit number");
      return;
    }
    //This is just to make sure we removed the mask from FE
    String localPhoneNumber = phoneNumber.replaceAll("\\D", "");
    if (!StringUtils.isNumeric(localPhoneNumber) || StringUtils.length(localPhoneNumber) != 10) {
      errors.rejectValue(field, "The phone number must be a 10-digit number");
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
      errors.rejectValue(field, String.format("%s has an invalid value [%s]", field, value));
      return false;
    }
    return true;
  }

  public static ClientTypeEnum getClientType(LegalTypeEnum legalType) {
    if (legalType == null) {
      return null;
    }
    return switch (legalType) {
      case A, B, BC, C, CP, EPR, FOR, LIC, REG -> ClientTypeEnum.C;
      case S, XS -> ClientTypeEnum.S;
      case XCP -> ClientTypeEnum.A;
      case SP -> ClientTypeEnum.RSP;
      case GP -> ClientTypeEnum.P;
      case LP, XL, XP -> ClientTypeEnum.L;
    };
  }
}
