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

@DisplayName("Unit Tests | AddressEmailValidator")
class AddressEmailValidatorTest {

  private final AddressEmailValidator validator = new AddressEmailValidator();

  @ParameterizedTest
  @MethodSource("ca.bc.gov.app.validator.address.AddressAddressValidatorTest#staffSource")
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
                new ClientAddressDto(
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    null,
                    null,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    email,
                    StringUtils.EMPTY,
                    0,
                    StringUtils.EMPTY
                ), 0
            )
            .as(StepVerifier::create);

    if (StringUtils.isNotBlank(expectedMessage)) {
      validation.expectNext(new ValidationError(
              "location.addresses[0].emailAddress",
              expectedMessage
          )
      );
    }

    validation.verifyComplete();

  }

  private static Stream<Arguments> validation() {
    return
        Stream.of(
            Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY),
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