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

@DisplayName("Unit Tests | ContactNameValidator")
class ContactNameValidatorTest {

  private final ContactNameValidator validator = new ContactNameValidator();

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
      String firstName,
      String lastName,
      String name,
      String expectedMessage
  ) {

    StepVerifier.FirstStep<ValidationError> validation =
        validator.validate(
                new ClientContactDto(
                    null,
                    firstName,
                    lastName,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
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
    return
        Stream.of(
            Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY, "firstName",
                "All contacts must have a first name."),
            Arguments.of("John", StringUtils.EMPTY, "lastName",
                "All contacts must have a last name."),
            Arguments.of("Mainé", StringUtils.EMPTY, "firstName",
                "Mainé has an invalid character."),
            Arguments.of("John", "Mainé", "lastName", "Mainé has an invalid character."),
            Arguments.of("John".repeat(20), StringUtils.EMPTY, "firstName",
                "This field has a 30 character limit."),
            Arguments.of("John", "Wick".repeat(20), "lastName",
                "This field has a 30 character limit."),
            Arguments.of("John", "Wick", StringUtils.EMPTY, StringUtils.EMPTY)
        );
  }
}