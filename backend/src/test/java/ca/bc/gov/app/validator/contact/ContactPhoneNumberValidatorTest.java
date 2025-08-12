package ca.bc.gov.app.validator.contact;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

@DisplayName("Unit Tests | ContactPhoneNumberValidator")
class ContactPhoneNumberValidatorTest {

  private final ContactPhoneNumberValidator validator = new ContactPhoneNumberValidator();

  @ParameterizedTest
  @MethodSource("ca.bc.gov.app.validator.address.AddressAddressValidatorTest#validSources")
  @DisplayName("Should support all validation sources")
  void shouldSupportAllValidationSources(ValidationSourceEnum source, boolean support) {
    assertEquals(support, validator.supports(source));
  }

  @ParameterizedTest
  @MethodSource("validation")
  @DisplayName("Should run validate")
  void shouldValidate(
      String phone1,
      String phone2,
      String fax,
      String name,
      String expectedMessage
  ) {

    StepVerifier.FirstStep<ValidationError> validation =
        validator.validate(
                new ClientContactDto(
                    null,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    phone1,
                    phone2,
                    fax,
                    StringUtils.EMPTY,
                    0,
                    List.of()
                ), 0
            )
            .as(StepVerifier::create);

    if (StringUtils.isNotBlank(expectedMessage)) {
      validation.expectNext(new ValidationError(
              "location.contacts[0]." + name,
              expectedMessage
          )
      );
    }

    validation.verifyComplete();

  }

  private static Stream<Arguments> validation() {
    String validPhone = "(250) 250 2550";
    String invalidPhone = "(250) 2550 2550";
    return
        Stream.of(
            Arguments.of(validPhone, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
                StringUtils.EMPTY),
            Arguments.of(validPhone, validPhone, StringUtils.EMPTY, StringUtils.EMPTY,
                StringUtils.EMPTY),
            Arguments.of(validPhone, StringUtils.EMPTY, validPhone, StringUtils.EMPTY,
                StringUtils.EMPTY),
            Arguments.of(invalidPhone, StringUtils.EMPTY, StringUtils.EMPTY, "phoneNumber",
                "The phone number must be a 10-digit number"),
            Arguments.of(validPhone, invalidPhone, StringUtils.EMPTY, "secondaryPhoneNumber",
                "The phone number must be a 10-digit number"),
            Arguments.of(validPhone, StringUtils.EMPTY, invalidPhone, "faxNumber",
                "The phone number must be a 10-digit number")
        );
  }
}