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

@DisplayName("Unit Tests | AddressCityValidator")
class AddressCityValidatorTest {

  private final AddressCityValidator validator = new AddressCityValidator();

  @ParameterizedTest
  @MethodSource("ca.bc.gov.app.validator.address.AddressAddressValidatorTest#validSources")
  @DisplayName("Should support all validation sources")
  void shouldSupportAllValidationSources(ValidationSourceEnum source, boolean support) {
    assertEquals(support, validator.supports(source));
  }

  @ParameterizedTest
  @MethodSource("validation")
  @DisplayName("Should run validate")
  void shouldValidate(String city, String expectedMessage) {

    StepVerifier.FirstStep<ValidationError> validation =
        validator.validate(
                new ClientAddressDto(
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    null,
                    null,
                    city,
                    StringUtils.EMPTY,
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
              "location.addresses[0].city",
              expectedMessage
          )
      );
    }

    validation.verifyComplete();

  }

  private static Stream<Arguments> validation() {
    return
        Stream.of(
            Arguments.of(StringUtils.EMPTY, "You must enter the name of a city or town."),
            Arguments.of("Mainé", "Mainé has an invalid character."),
            Arguments.of("Llanfairpwllgwyngyllgogerychwyrndrobwllllantysiliogogogoch",
                "This field has a 30 character limit."),
            Arguments.of("Main5", StringUtils.EMPTY)
        );
  }


}