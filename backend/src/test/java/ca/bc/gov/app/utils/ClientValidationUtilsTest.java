package ca.bc.gov.app.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientTypeEnum;
import ca.bc.gov.app.dto.client.LegalTypeEnum;
import ca.bc.gov.app.util.ClientValidationUtils;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

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

  @ParameterizedTest(name = "For phone number {0} error message is {1}")
  @MethodSource("validatePhoneNumberContents")
  @DisplayName("validating phone numbers")
  void shouldValidatePhoneNumbers(String phoneNumber, String errorMessage) {
    String field = "phoneNumber";

    StepVerifier.FirstStep<ValidationError> validation =
        ClientValidationUtils.validatePhoneNumber(field, phoneNumber)
            .as(StepVerifier::create);

    if (StringUtils.isNotBlank(errorMessage)) {
      validation.expectNext(new ValidationError(field, errorMessage));
    }
    validation.verifyComplete();

  }


  @ParameterizedTest(name = "For email {0} error message is {1}")
  @MethodSource("validateEmailContents")
  @DisplayName("validating emails")
  void shouldValidateEmails(String email, String errorMessage) {
    String field = "email";

    StepVerifier.FirstStep<ValidationError> validation =
        ClientValidationUtils
            .validateEmail(email, field)
            .as(StepVerifier::create);

    if (StringUtils.isNotBlank(errorMessage)) {
      validation.expectNext(new ValidationError(field, errorMessage));
    }

    validation.verifyComplete();
  }

  @ParameterizedTest(name = "For note {0} error message is {1}")
  @MethodSource("validateNotesContents")
  @DisplayName("validating notes")
  void shouldValidateNotes(String note, String errorMessage) {
    String field = "note";

    StepVerifier.FirstStep<ValidationError> validation =
        ClientValidationUtils
            .validateNotes(note, field)
            .as(StepVerifier::create);

    if (StringUtils.isNotBlank(errorMessage)) {
      validation.expectNext(new ValidationError(field, errorMessage));
    }

    validation.verifyComplete();
  }

  private static Stream<Arguments> validatePhoneNumberContents() {
    return Stream.of(Arguments.of("1234567890", StringUtils.EMPTY),
        Arguments.of(StringUtils.EMPTY, "The phone number must be a 10-digit number"),
        Arguments.of("abc", "The phone number must be a 10-digit number"),
        Arguments.of("123", "The phone number must be a 10-digit number"),
        Arguments.of("(123) 4567 8901", "The phone number must be a 10-digit number"),
        Arguments.of("(123) 456 7890", StringUtils.EMPTY)
    );
  }

  private static Stream<Arguments> validateEmailContents() {
    return
        Stream.of(
            Arguments.of("jhon@email.ca", StringUtils.EMPTY),
            Arguments.of("jhon", "You must enter an email address in a valid format. "
                + "For example: name@example.com"),
            Arguments.of(StringUtils.EMPTY, "You must enter an email address"),
            Arguments.of("lucyintheskieswithdiamondsisoneofthebeatlessongsfromsgtpeperslonelyheartsclubbandalbum@thebeatlesbandabbeyroad.co.ok", "This field has a 100 character limit.")
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
            Arguments.of(LegalTypeEnum.SP, ClientTypeEnum.RSP),
            Arguments.of(LegalTypeEnum.GP, ClientTypeEnum.P),
            Arguments.of(LegalTypeEnum.LP, ClientTypeEnum.L),
            Arguments.of(LegalTypeEnum.LL, ClientTypeEnum.L),
            Arguments.of(LegalTypeEnum.XL, ClientTypeEnum.L),
            Arguments.of(LegalTypeEnum.XP, ClientTypeEnum.L),
            Arguments.of(null, null)
        );
  }

  private static Stream<Arguments> validateNotesContents() {
    return Stream.of(
        Arguments.of("This is a note", StringUtils.EMPTY),
        Arguments.of("Esta é uma nota em português brasileiro", "notes has an invalid character."),
        Arguments.of("this is a big note".repeat(500), "This field has a 4000 character limit.")
    );
  }

}