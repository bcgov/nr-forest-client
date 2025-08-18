package ca.bc.gov.app.validator.business;

import static ca.bc.gov.app.util.ClientValidationUtils.fieldIsMissingErrorMessage;
import static org.junit.jupiter.api.Assertions.*;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.MDC;
import reactor.test.StepVerifier;

@DisplayName("Unit Tests | BusinessInformationBirthdayValidator")
class BusinessInformationBirthdayValidatorTest {

  private final BusinessInformationBirthdayValidator validator = new BusinessInformationBirthdayValidator();

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
      LocalDate birthdate,
      String expectedMessage,
      String role
  ) {
    MDC.put(ApplicationConstant.MDC_USERROLES,role);

    StepVerifier.FirstStep<ValidationError> validation =
        validator.validate(
                new ClientBusinessInformationDto(
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "I",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    birthdate,
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
              "businessInformation.birthdate",
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
                StringUtils.EMPTY,
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
                StringUtils.EMPTY
            ), 0
        )
        .as(StepVerifier::create)
        .verifyComplete();

  }

  private static Stream<Arguments> validation() {
    return
        Stream.of(
            Arguments.of(null, fieldIsMissingErrorMessage("Birthdate"), ApplicationConstant.ROLE_EDITOR),
            Arguments.of(LocalDate.now(), "Sole proprietorship must be at least 19 years old", ApplicationConstant.ROLE_EDITOR),
            Arguments.of(LocalDate.now().minusYears(22), StringUtils.EMPTY, ApplicationConstant.ROLE_EDITOR),
            Arguments.of(null, StringUtils.EMPTY, ApplicationConstant.ROLE_ADMIN)
        );
  }

}