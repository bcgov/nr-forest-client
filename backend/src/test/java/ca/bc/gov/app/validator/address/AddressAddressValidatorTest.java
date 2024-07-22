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

@DisplayName("Unit Tests | AddressAddressValidator")
class AddressAddressValidatorTest {

  private final AddressAddressValidator validator = new AddressAddressValidator();

  @ParameterizedTest
  @MethodSource("validSources")
  @DisplayName("Should support all validation sources")
  void shouldSupportAllValidationSources(ValidationSourceEnum source, boolean support) {
    assertEquals(support, validator.supports(source));
  }

  @ParameterizedTest
  @MethodSource("validation")
  @DisplayName("Should run validate")
  void shouldValidate(String address, String expectedMessage) {

    StepVerifier.FirstStep<ValidationError> validation =
        validator.validate(
                new ClientAddressDto(
                    address,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
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
              "location.addresses[0].streetAddress",
              expectedMessage
          )
      );
    }

    validation.verifyComplete();

  }

  public static Stream<Arguments> staffSource() {
    return Stream.of(
        Arguments.of(ValidationSourceEnum.STAFF, true),
        Arguments.of(ValidationSourceEnum.EXTERNAL, false),
        Arguments.of(null, false)
    );
  }

  public static Stream<Arguments> externalSource() {
    return Stream.of(
        Arguments.of(ValidationSourceEnum.STAFF, false),
        Arguments.of(ValidationSourceEnum.EXTERNAL, true),
        Arguments.of(null, false)
    );
  }

  public static Stream<Arguments> validSources() {
    return Stream.of(
        Arguments.of(ValidationSourceEnum.STAFF, true),
        Arguments.of(ValidationSourceEnum.EXTERNAL, true),
        Arguments.of(null, true)
    );
  }

  private static Stream<Arguments> validation() {
    return
        Stream.of(
            Arguments.of(StringUtils.EMPTY, "You must enter a street address or PO box number."),
            Arguments.of("1234 Mainé St", "1234 Mainé St has an invalid character."),
            Arguments.of("Avenida Bailarina Selma Parada 505, Sala 12 Conjunto 5",
                "This field has a 40 character limit."),
            Arguments.of("1234 Main St", StringUtils.EMPTY)
        );
  }

}