package ca.bc.gov.app.validator.location;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

@DisplayName("Unit Tests | LocationAssociationValidator")
class LocationAssociationValidatorTest {

  private final LocationAssociationValidator validator = new LocationAssociationValidator();

  @ParameterizedTest
  @MethodSource("ca.bc.gov.app.validator.address.AddressAddressValidatorTest#validSources")
  @DisplayName("Should support all validation sources")
  void shouldSupportAllValidationSources(ValidationSourceEnum source, boolean support) {
    assertEquals(support, validator.supports(source));
  }

  @ParameterizedTest
  @MethodSource("validation")
  @DisplayName("Should run validate")
  void shouldValidate(List<ClientValueTextDto> association, String expectedMessage) {

    ClientLocationDto target = new ClientLocationDto(
        List.of(
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
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                0,
                "Main Address"
            )
        ),
        List.of(
            new ClientContactDto(
                null,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                0,
                association
            )
        )
    );

    StepVerifier.FirstStep<ValidationError> validation =
        validator.validate(target, 0)
            .as(StepVerifier::create);

    if (StringUtils.isNotBlank(expectedMessage)) {
      validation.expectNext(new ValidationError(
              "location.contacts[0].locationNames",
              expectedMessage
          )
      );
    }

    validation.verifyComplete();

  }

  private static Stream<Arguments> validation() {
    return
        Stream.of(
            Arguments.of(null, "Contact has no locations associated to it"),
            Arguments.of(List.of(), "Contact has no locations associated to it"),
            Arguments.of(List.of(new ClientValueTextDto("", "")),
                "Contact has no locations associated to it"),
            Arguments.of(List.of(new ClientValueTextDto("0", "Billing Address")),
                "Contact has invalid address association"),
            Arguments.of(List.of(new ClientValueTextDto("0", "Main Address")), StringUtils.EMPTY)
        );
  }

}