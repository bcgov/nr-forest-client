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

@DisplayName("Unit Tests | ContactEmailValidator")
class ContactEmailValidatorTest {

  private final ContactEmailValidator validator = new ContactEmailValidator();

  @ParameterizedTest
  @MethodSource("ca.bc.gov.app.validator.address.AddressAddressValidatorTest#validSources")
  @DisplayName("Should support all validation sources")
  void shouldSupportAllValidationSources(ValidationSourceEnum source, boolean support) {
    assertEquals(support, validator.supports(source));
  }

  @ParameterizedTest
  @MethodSource("validation")
  @DisplayName("Should run validate")
  void shouldValidate(String email, String expectedMessage) {

    StepVerifier.FirstStep<ValidationError> validation =
        validator.validate(
                new ClientContactDto(
                    null,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    email,
                    0,
                    List.of()
                ), 0
            )
            .as(StepVerifier::create);

    if (StringUtils.isNotBlank(expectedMessage)) {
      validation.expectNext(new ValidationError(
              "location.contacts[0].email",
              expectedMessage
          )
      );
    }

    validation.verifyComplete();

  }

  private static Stream<Arguments> validation() {
    return
        Stream.of(
            Arguments.of("Mainé",
                "You must enter an email address in a valid format. For example: name@example.com"),
            Arguments.of("Llama".repeat(50), "This field has a 100 character limit."),
            Arguments.of("Main5",
                "You must enter an email address in a valid format. For example: name@example.com"),
            Arguments.of("thatsnotme@",
                "You must enter an email address in a valid format. For example: name@example.com"),
            Arguments.of("thatsnotme@mail",
                "You must enter an email address in a valid format. For example: name@example.com"),
            Arguments.of("thatsnotme@mail.thisisnotdomain",
                "You must enter an email address in a valid format. For example: name@example.com"),
            Arguments.of("thatsnotme@mail.ca", StringUtils.EMPTY)
        );
  }
  
}