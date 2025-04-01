package ca.bc.gov.app.validator.business;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

@DisplayName("Unit Tests | BusinessInformationIndividualNameValidator")
class BusinessInformationIndividualNameValidatorTest {

  private final BusinessInformationIndividualNameValidator validator = new BusinessInformationIndividualNameValidator();

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
      String firstName,
      String middleName,
      String lastName,
      String name,
      String expectedMessage
  ) {

    StepVerifier.FirstStep<ValidationError> validation =
        validator.validate(
                new ClientBusinessInformationDto(
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "U",
                    "I",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    null,

                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    firstName,
                    middleName,
                    lastName,
                    StringUtils.EMPTY,
                    null,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY
                ), 0
            )
            .as(StepVerifier::create);

    if (StringUtils.isNotBlank(expectedMessage)) {
      validation.expectNext(new ValidationError(
              "businessInformation." + name,
              expectedMessage
          )
      );
    }

    validation.verifyComplete();

  }

  @Test
  @DisplayName("Should run validate")
  void shouldValidate() {

    validator.validate(
            new ClientBusinessInformationDto(
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                "U",
                "C",
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                null,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                null,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY
            ), 0
        )
        .as(StepVerifier::create)
        .verifyComplete();

  }

  private static Stream<Arguments> validation() {
    return
        Stream.of(
            Arguments.of("John", StringUtils.EMPTY, "Wick", StringUtils.EMPTY, StringUtils.EMPTY),
            Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, "firstName",
                "You must enter a first name"),
            Arguments.of("John", StringUtils.EMPTY, StringUtils.EMPTY, "lastName",
                "You must enter a last name"),
            Arguments.of("Mainé", StringUtils.EMPTY, "Wick", "firstName",
                "Mainé has an invalid character."),
            Arguments.of("John".repeat(10), StringUtils.EMPTY, "Wick", "firstName",
                "This field has a 30 character limit."),
            Arguments.of("John", StringUtils.EMPTY, "Mainé", "lastName",
                "Mainé has an invalid character."),
            Arguments.of("John", StringUtils.EMPTY, "Wick".repeat(10), "lastName",
                "This field has a 30 character limit."),
            Arguments.of("John", "Mainé", "Wick", "middleName", "Mainé has an invalid character."),
            Arguments.of("John", "Valaise".repeat(10), "Wick", "middleName",
                "This field has a 30 character limit.")
        );
  }

}