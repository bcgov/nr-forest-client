package ca.bc.gov.app.util;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.bc.gov.app.dto.client.ClientTypeEnum;
import ca.bc.gov.app.dto.client.LegalTypeEnum;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

@DisplayName("Unit Test | Client Validation")
class ClientValidationUtilsTest {

  @ParameterizedTest
  @MethodSource("getClientType")
  @DisplayName("getClientType and it's values")
  void shouldGetClientType(LegalTypeEnum legal, ClientTypeEnum client) {
    if (client != null) {
      ClientTypeEnum clientResult = ClientValidationUtils.getClientType(legal);
      assertNotNull(clientResult);
      assertEquals(client, clientResult);
    } else {
      assertNull(ClientValidationUtils.getClientType(legal));
    }
  }

  @ParameterizedTest(name = "For phone number {0} I will have {1} errors with message {2}")
  @MethodSource("validatePhoneNumberContents")
  @DisplayName("validating phone numbers")
  void shouldValidatePhoneNumbers(String phoneNumber, int errorCount, String errorMessage) {
    String field = "phoneNumber";
    Map<String, Object> target = new HashMap<>();
    Errors errors = new MapBindingResult(target, "");

    ClientValidationUtils.validatePhoneNumber(phoneNumber, field, errors);

    System.out.println("Expected Error Count: " + errorCount);
    System.out.println("Actual Error Count: " + errors.getErrorCount());

    assertEquals(errorCount, errors.getErrorCount());
    if (errorCount > 0) {
      System.out.println("Errors:");
      for (ObjectError error : errors.getAllErrors()) {
        System.out.println(error.getDefaultMessage());
      }

      assertThat(
          errors
              .getAllErrors()
              .stream()
              .map(x -> (FieldError) x)
              .map(DefaultMessageSourceResolvable::getCodes)
              .flatMap(Stream::of)
      )
          .isNotNull()
          .hasSizeBetween(2, 3)
          .contains(errorMessage);
    }
  }

  private static Stream<Arguments> validatePhoneNumberContents() {
    return Stream.of(Arguments.of("1234567890", 0, StringUtils.EMPTY),
        Arguments.of(StringUtils.EMPTY, 1, "The phone number must be a 10-digit number"),
        Arguments.of("abc", 1, "The phone number must be a 10-digit number"),
        Arguments.of("123", 1, "The phone number must be a 10-digit number")
    );
  }

  @ParameterizedTest(name = "For email {0} I will have {1} errors with message {2}")
  @MethodSource("validateEmailContents")
  @DisplayName("validating emails")
  void shouldValidateEmails(String email, int errorCount, String errorMessage) {
    String field = "email";
    Map<String, Object> target = new HashMap<>();
    Errors errors = new MapBindingResult(target, "objectName");

    ClientValidationUtils.validateEmail(email, field, errors);

    assertEquals(errorCount, errors.getErrorCount());
    if (errorCount > 0) {
      assertThat(
          errors
              .getAllErrors()
              .stream()
              .map(x -> (FieldError) x)
              .map(DefaultMessageSourceResolvable::getCodes)
              .flatMap(Stream::of)
      )
          .isNotNull()
          .hasSize(3)
          .contains(errorMessage);
    }
  }

  @Test
  @DisplayName("should check if enum is valid")
  void shouldCheckIfEnumIsValid() {
    Map<String, Object> target = new HashMap<>();
    Errors errors = new MapBindingResult(target, "");

    assertTrue(
        ClientValidationUtils.isValidEnum("A", "legalType", LegalTypeEnum.class, errors)
    );

  }

  private static Stream<Arguments> validateEmailContents() {
    return
        Stream.of(
            Arguments.of("jhon@email.ca", 0, StringUtils.EMPTY),
            Arguments.of("jhon", 1, "You must enter an email address in a valid format. "
                                    + "For example: name@example.com"),
            Arguments.of(StringUtils.EMPTY, 1, "You must enter an email address")
        );
  }

  private static Stream<Arguments> getClientType() {
    return
        Stream.of(
            Arguments.of(LegalTypeEnum.A, ClientTypeEnum.C),
            Arguments.of(LegalTypeEnum.B, ClientTypeEnum.C),
            Arguments.of(LegalTypeEnum.BC, ClientTypeEnum.C),
            Arguments.of(LegalTypeEnum.C, ClientTypeEnum.C),
            Arguments.of(LegalTypeEnum.CP, ClientTypeEnum.C),
            Arguments.of(LegalTypeEnum.EPR, ClientTypeEnum.C),
            Arguments.of(LegalTypeEnum.FOR, ClientTypeEnum.C),
            Arguments.of(LegalTypeEnum.LIC, ClientTypeEnum.C),
            Arguments.of(LegalTypeEnum.REG, ClientTypeEnum.C),
            Arguments.of(LegalTypeEnum.S, ClientTypeEnum.S),
            Arguments.of(LegalTypeEnum.XS, ClientTypeEnum.S),
            Arguments.of(LegalTypeEnum.XCP, ClientTypeEnum.A),
            Arguments.of(LegalTypeEnum.SP, ClientTypeEnum.I),
            Arguments.of(LegalTypeEnum.GP, ClientTypeEnum.P),
            Arguments.of(LegalTypeEnum.LP, ClientTypeEnum.L),
            Arguments.of(LegalTypeEnum.XL, ClientTypeEnum.L),
            Arguments.of(LegalTypeEnum.XP, ClientTypeEnum.L),
            Arguments.of(null, null)
        );
  }

}