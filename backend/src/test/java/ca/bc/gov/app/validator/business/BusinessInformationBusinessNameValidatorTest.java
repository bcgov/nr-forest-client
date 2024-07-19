package ca.bc.gov.app.validator.business;

import static org.junit.jupiter.api.Assertions.*;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

@DisplayName("Unit Tests | BusinessInformationBusinessNameValidator")
class BusinessInformationBusinessNameValidatorTest {

  private final BusinessInformationBusinessNameValidator validator = new BusinessInformationBusinessNameValidator();

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
      String businessName,
      String businessType,
      String expectedMessage
  ) {

    StepVerifier.FirstStep<ValidationError> validation =
        validator.validate(
                new ClientBusinessInformationDto(
                    StringUtils.EMPTY,
                    businessName,
                    businessType,
                    StringUtils.EMPTY,
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
                    StringUtils.EMPTY
                ), 0
            )
            .as(StepVerifier::create);

    if (StringUtils.isNotBlank(expectedMessage)) {
      validation.expectNext(new ValidationError(
              "businessInformation.businessName",
              expectedMessage
          )
      );
    }

    validation.verifyComplete();

  }

  private static Stream<Arguments> validation() {
    return
        Stream.of(
            Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY, "You must enter a business name."),
            Arguments.of(StringUtils.EMPTY, "R", "You must select your B.C. registered business name from the list."),
            Arguments.of("Mainé", StringUtils.EMPTY, "Mainé has an invalid character."),
            Arguments.of("Corporation".repeat(6), StringUtils.EMPTY, "This field has a 60 character limit."),
            Arguments.of("Corporation", "U", "Business name must be composed of first and last name"),
            Arguments.of("Corporation", "R", StringUtils.EMPTY)
        );
  }

}