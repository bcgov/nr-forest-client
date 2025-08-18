package ca.bc.gov.app.util;

import static java.util.function.Predicate.not;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientTypeEnum;
import ca.bc.gov.app.dto.client.LegalTypeEnum;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientValidationUtils {

  private static final String EMAIL_REGEX = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$";
  private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
  public static final Pattern US7ASCII_PATTERN = Pattern.compile("^[\\x00-\\x7F]+$");

  public static Mono<ValidationError> validateEmail(String email, String field) {
    if (StringUtils.isBlank(email)) {
      return Mono.just(new ValidationError(field, "You must enter an email address"));
    }

    if (StringUtils.length(email) > 100) {
      return Mono.just(new ValidationError(field, "This field has a 100 character limit."));
    }

    return
        Mono
            .just(email)
            .map(EMAIL_PATTERN::matcher)
            .filter(not(Matcher::matches))
            .map(matcher ->
                new ValidationError(
                    field,
                    "You must enter an email address in a valid format. For example: name@example.com"
                )
            );
  }

  public static Mono<ValidationError> validatePhoneNumber(String field,String phoneNumber) {
    if (StringUtils.isBlank(phoneNumber)) {
      return Mono.just(new ValidationError(field, "The phone number must be a 10-digit number"));
    }
    //This is just to make sure we removed the mask from FE
    String localPhoneNumber = phoneNumber.replaceAll("\\D", "");
    if (!StringUtils.isNumeric(localPhoneNumber) || StringUtils.length(localPhoneNumber) != 10) {
      return Mono.just(new ValidationError(field, "The phone number must be a 10-digit number"));
    }
    return Mono.empty();
  }

  public static Mono<ValidationError> validateNotes(String notes, String field) {
    return validateBySize(notes, field, 4000, 0, "notes");
  }

  public static Mono<ValidationError> validateBySize(
      String value,
      String field,
      int maxSize,
      int minSize,
      String name
  ) {

    if (StringUtils.isEmpty(value)) {
      return Mono.empty();
    }

    if (StringUtils.length(value) > maxSize) {
      return Mono.just(new ValidationError(field, "This field has a "+maxSize+" character limit."));
    }

    if (StringUtils.length(value) < minSize) {
      return Mono.just(new ValidationError(field, "This field should have at least "+minSize+" characters."));
    }

    if (!US7ASCII_PATTERN.matcher(value).matches()) {
      return Mono.just(new ValidationError(field, name+" has an invalid character."));
    }
    return Mono.empty();
  }

  public static String fieldIsMissingErrorMessage(String fieldName) {
    return String.format("%s is missing", fieldName);
  }

  public static ClientTypeEnum getClientType(LegalTypeEnum legalType) {
    if (legalType == null) {
      return null;
    }
    return switch (legalType) {
      case A, B, BC, C, CP, EPR, FOR, LIC, REG, ULC -> ClientTypeEnum.C;
      case S, XS -> ClientTypeEnum.S;
      case XCP -> ClientTypeEnum.A;
      case SP -> ClientTypeEnum.RSP;
      case GP -> ClientTypeEnum.P;
      case LL, LP, XL, XP -> ClientTypeEnum.L;
    };
  }
}
