package ca.bc.gov.app.validator.address;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

@DisplayName("Unit Tests | AddressPostalCodeValidator")
class AddressPostalCodeValidatorTest {

  private final AddressPostalCodeValidator validator = new AddressPostalCodeValidator();

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
      String postalCode,
      String countryCode,
      String expectedMessage
  ) {

    StepVerifier.FirstStep<ValidationError> validation =
        validator.validate(
                new ClientAddressDto(
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.isBlank(countryCode) ? null
                        : new ClientValueTextDto(countryCode, StringUtils.EMPTY),
                    null,
                    StringUtils.EMPTY,
                    postalCode,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    0,
                    StringUtils.EMPTY
                ), 0
            )
            .as(StepVerifier::create);

    if (StringUtils.isNotBlank(expectedMessage)) {
      validation.expectNext(new ValidationError(
              "location.addresses[0].postalCode",
              expectedMessage
          )
      );
    }

    validation.verifyComplete();

  }

  private static Stream<Arguments> validation() {
    return
        Stream.of(
            Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY),
            Arguments.of("V8W1A1", "CA", StringUtils.EMPTY),
            Arguments.of("04110", "US", StringUtils.EMPTY),
            Arguments.of("05130-0201", "US", StringUtils.EMPTY),
            Arguments.of("09450-000", "BR", StringUtils.EMPTY),
            Arguments.of("09450000", "BR", StringUtils.EMPTY),
            Arguments.of("09450000", StringUtils.EMPTY, StringUtils.EMPTY),
            Arguments.of(StringUtils.EMPTY, "CA",
                "You must include a postal code in the format A9A9A9."),
            Arguments.of(StringUtils.EMPTY, "US",
                "You must include a zip code in the format 000000 or 00000-0000."),
            Arguments.of(StringUtils.EMPTY, "BR",
                "You must include a postal code up to 10 characters."),
            Arguments.of("V8W1A1A", "CA", "has more than 6 characters"),
            Arguments.of("123456", "CA", "invalid Canadian postal code format"),
            Arguments.of("041104-000", "US", "invalid US zip code format"),
            Arguments.of("09450000457", "BR", "has more than 10 characters")
        );
  }
}