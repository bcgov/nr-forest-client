package ca.bc.gov.app.validator.address;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

@DisplayName("Unit Tests | AddressPhoneNumberValidator")
class AddressPhoneNumberValidatorTest {

  private final AddressPhoneNumberValidator validator = new AddressPhoneNumberValidator();

  @ParameterizedTest
  @MethodSource("ca.bc.gov.app.validator.address.AddressAddressValidatorTest#staffSource")
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
                new ClientAddressDto(
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    null,
                    null,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    phone1,
                    phone2,
                    fax,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    0,
                    StringUtils.EMPTY
                ), 0
            )
            .as(StepVerifier::create);

    if (StringUtils.isNotBlank(expectedMessage)) {
      validation.expectNext(new ValidationError(
              "location.addresses[0]." + name,
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
            Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
                StringUtils.EMPTY),
            Arguments.of(validPhone, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
                StringUtils.EMPTY),
            Arguments.of(StringUtils.EMPTY, validPhone, StringUtils.EMPTY, StringUtils.EMPTY,
                StringUtils.EMPTY),
            Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY, validPhone, StringUtils.EMPTY,
                StringUtils.EMPTY),
            Arguments.of(invalidPhone, StringUtils.EMPTY, StringUtils.EMPTY, "businessPhoneNumber",
                "The phone number must be a 10-digit number"),
            Arguments.of(StringUtils.EMPTY, invalidPhone, StringUtils.EMPTY, "secondaryPhoneNumber",
                "The phone number must be a 10-digit number"),
            Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY, invalidPhone, "faxNumber",
                "The phone number must be a 10-digit number")
        );
  }
}