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

@DisplayName("Unit Tests | AddressComplementaryInformationValidator")
class AddressComplementaryInformationValidatorTest {

  private final AddressComplementaryInformationValidator validator = new AddressComplementaryInformationValidator();

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
      String complementaryOne,
      String complementaryTwo,
      String name,
      String expectedMessage
  ) {

    StepVerifier.FirstStep<ValidationError> validation =
        validator.validate(
                new ClientAddressDto(
                    StringUtils.EMPTY,
                    complementaryOne,
                    complementaryTwo,
                    null,
                    null,
                    StringUtils.EMPTY,
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
              "location.addresses[0].complementaryAddress" + name,
              expectedMessage
          )
      );
    }

    validation.verifyComplete();

  }

  private static Stream<Arguments> validation() {
    return Stream.of(
        Arguments.of(StringUtils.EMPTY, 
                     StringUtils.EMPTY, 
                     StringUtils.EMPTY, 
                     StringUtils.EMPTY),
        Arguments.of(StringUtils.EMPTY, 
                     "Something", 
                     "One",
                     "You must enter the first complementary address."),
        Arguments.of("Something é", 
                     StringUtils.EMPTY, 
                     "One",
                     "Something é has an invalid character."),
        Arguments.of("Potato".repeat(10), 
                     StringUtils.EMPTY, 
                     "One",
                     "The address must be between 4 and 40 characters."),
        Arguments.of("Potato", 
                     "Something é", 
                     "Two", 
                     "Something é has an invalid character."),
        Arguments.of("Potato", 
                     "Potato".repeat(10), 
                     "Two",
                     "The address must be between 4 and 40 characters."));
  }

}