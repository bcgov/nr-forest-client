package ca.bc.gov.app.validator.business;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

/**
 * Unit tests for {@link BusinessInformationIdentificationDocumentValidator}.
 */
@DisplayName("Unit Tests | BusinessInformationIdentificationDocumentValidator")
class BusinessInformationIdentificationDocumentValidatorTest {

  private final BusinessInformationIdentificationDocumentValidator validator =
      new BusinessInformationIdentificationDocumentValidator();

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
      String documentType,
      String documentCountry,
      String documentProvince,
      String documentNumber,
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
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    new ClientValueTextDto(documentType, StringUtils.EMPTY),
                    documentNumber,
                    documentCountry,
                    documentProvince
                ),
                0
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
                StringUtils.EMPTY
            ), 0
        )
        .as(StepVerifier::create)
        .verifyComplete();

  }
  
  private static Stream<Arguments> validation() {
    String idTypeField = "identificationType";
    String clientIdField = "clientIdentification";

    String typeError = "You must select an identification type";
    String enterError = "You must enter an identification number";
    String birthError = "The birth certificate must be a 12 or 13-digit number";
    String bcdlError = "The driver's license must be a 7 or 8-digit number";
    String passError =
        "The passport must follow the format: two letters followed by 6 or 7 digits "
            + "(e.g., AB123456 or AB1234567)";
    String citzError = "The Canadian Citizenship must be a 8-digit number";
    String fnidError = "The First Nations ID must be a 10-digit number";
    String bcidError = "The British Columbia Identification Card must be an 8-digit number";
    String prcdError =
        "The permanent resident card number must start with two letters followed by either 10 "
            + "or 7 digits (e.g., RA0302123456 or RA1234567)";
    String othrLenError = "The other document must be between 3 and 40 characters long";
    String othrPatternError =
        "Other identification must follow the pattern: [ID Type] : [ID Value] "
            + "such as 'USA Passport : 12345'";
    String dlError =
        "The driver's license must be between 7 and 20 characters and contain only letters "
            + "and numbers";

    return Stream.of(
        Arguments.of(StringUtils.EMPTY, "CA", "BC", "123456789", idTypeField, typeError),
        Arguments.of("CCTV", StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, idTypeField,
            typeError),
        Arguments.of("CDDL", "CA", "BC", StringUtils.EMPTY, clientIdField, enterError),

        Arguments.of("BRTH", StringUtils.EMPTY, StringUtils.EMPTY, "1", clientIdField, birthError),
        Arguments.of("BRTH", StringUtils.EMPTY, StringUtils.EMPTY, "12345678901234", clientIdField,
            birthError),
        Arguments.of("BRTH", StringUtils.EMPTY, StringUtils.EMPTY, "potato", clientIdField,
            birthError),
        Arguments.of("BRTH", StringUtils.EMPTY, StringUtils.EMPTY, "123456789012",
            StringUtils.EMPTY, StringUtils.EMPTY),

        Arguments.of("CDDL", "CA", "BC", "123456789", clientIdField, bcdlError),
        Arguments.of("CDDL", "CA", "BC", "123", clientIdField, bcdlError),
        Arguments.of("CDDL", "CA", "BC", "potato", clientIdField, bcdlError),
        Arguments.of("CDDL", "CA", "BC", "12345678", StringUtils.EMPTY, StringUtils.EMPTY),

        Arguments.of("PASS", StringUtils.EMPTY, StringUtils.EMPTY, "!A1", clientIdField, passError),
        Arguments.of("PASS", StringUtils.EMPTY, StringUtils.EMPTY, "12345678901234", clientIdField,
            passError),
        Arguments.of("PASS", StringUtils.EMPTY, StringUtils.EMPTY, "potato", clientIdField,
            passError),
        Arguments.of("PASS", StringUtils.EMPTY, StringUtils.EMPTY, "12345678", clientIdField,
            passError),
        Arguments.of("PASS", StringUtils.EMPTY, StringUtils.EMPTY, "A123456BC", clientIdField,
            passError),
        Arguments.of("PASS", StringUtils.EMPTY, StringUtils.EMPTY, "xx123456", clientIdField,
            passError),
        Arguments.of("PASS", StringUtils.EMPTY, StringUtils.EMPTY, "XX123456",
            StringUtils.EMPTY, StringUtils.EMPTY),
        Arguments.of("PASS", StringUtils.EMPTY, StringUtils.EMPTY, "XX1234567",
            StringUtils.EMPTY, StringUtils.EMPTY),

        Arguments.of("CITZ", StringUtils.EMPTY, StringUtils.EMPTY, "A1", clientIdField, citzError),
        Arguments.of("CITZ", StringUtils.EMPTY, StringUtils.EMPTY, "12345678901234", clientIdField,
            citzError),
        Arguments.of("CITZ", StringUtils.EMPTY, StringUtils.EMPTY, "1", clientIdField, citzError),
        Arguments.of("CITZ", StringUtils.EMPTY, StringUtils.EMPTY, "12345678",
            StringUtils.EMPTY, StringUtils.EMPTY),

        Arguments.of("FNID", StringUtils.EMPTY, StringUtils.EMPTY, "A1", clientIdField, fnidError),
        Arguments.of("FNID", StringUtils.EMPTY, StringUtils.EMPTY, "12345678901234", clientIdField,
            fnidError),
        Arguments.of("FNID", StringUtils.EMPTY, StringUtils.EMPTY, "1", clientIdField, fnidError),
        Arguments.of("FNID", StringUtils.EMPTY, StringUtils.EMPTY, "1234567890",
            StringUtils.EMPTY, StringUtils.EMPTY),

        Arguments.of("BCID", StringUtils.EMPTY, StringUtils.EMPTY, "A1", clientIdField, bcidError),
        Arguments.of("BCID", StringUtils.EMPTY, StringUtils.EMPTY, "12345678901234", clientIdField,
            bcidError),
        Arguments.of("BCID", StringUtils.EMPTY, StringUtils.EMPTY, "1", clientIdField, bcidError),
        Arguments.of("BCID", StringUtils.EMPTY, StringUtils.EMPTY, "12345678",
            StringUtils.EMPTY, StringUtils.EMPTY),

        Arguments.of("PRCD", StringUtils.EMPTY, StringUtils.EMPTY, "AB1234567890",
            StringUtils.EMPTY, StringUtils.EMPTY),
        Arguments.of("PRCD", StringUtils.EMPTY, StringUtils.EMPTY, "AB1234567",
            StringUtils.EMPTY, StringUtils.EMPTY),
        Arguments.of("PRCD", StringUtils.EMPTY, StringUtils.EMPTY, "AB12345678", clientIdField,
            prcdError),
        Arguments.of("PRCD", StringUtils.EMPTY, StringUtils.EMPTY, "A12345678", clientIdField,
            prcdError),
        Arguments.of("PRCD", StringUtils.EMPTY, StringUtils.EMPTY, "ABC1234567890", clientIdField,
            prcdError),
        Arguments.of("PRCD", StringUtils.EMPTY, StringUtils.EMPTY, "ab1234567890",
            StringUtils.EMPTY, StringUtils.EMPTY),

        Arguments.of("OTHR", StringUtils.EMPTY, StringUtils.EMPTY, "A1", clientIdField,
            othrLenError),
        Arguments.of("OTHR", StringUtils.EMPTY, StringUtils.EMPTY, "A1".repeat(22), clientIdField,
            othrLenError),
        Arguments.of("OTHR", StringUtils.EMPTY, StringUtils.EMPTY, "12345", clientIdField,
            othrPatternError),
        Arguments.of("OTHR", StringUtils.EMPTY, StringUtils.EMPTY, "USA Passport : 1",
            clientIdField, othrPatternError),
        Arguments.of("OTHR", StringUtils.EMPTY, StringUtils.EMPTY, "USA Passport ; 12345",
            clientIdField, othrPatternError),
        Arguments.of("OTHR", StringUtils.EMPTY, StringUtils.EMPTY, "A : 1", clientIdField,
            othrPatternError),
        Arguments.of("OTHR", StringUtils.EMPTY, StringUtils.EMPTY, "USA Passport : 12345",
            StringUtils.EMPTY, StringUtils.EMPTY),

        Arguments.of("CDDL", "CA", "AB", "1".repeat(22), clientIdField, dlError),
        Arguments.of("CDDL", "CA", "NL", "123", clientIdField, dlError),
        Arguments.of("CDDL", "CA", "ON", "!A1", clientIdField, dlError),
        Arguments.of("CDDL", "CA", "SK", "AB12345678", StringUtils.EMPTY, StringUtils.EMPTY),

        Arguments.of("USDL", "US", "CA", "1".repeat(22), clientIdField, dlError),
        Arguments.of("USDL", "US", "CA", "123", clientIdField, dlError),
        Arguments.of("USDL", "US", "CA", "!A1", clientIdField, dlError),
        Arguments.of("USDL", "US", "CA", "CA12345678", StringUtils.EMPTY, StringUtils.EMPTY)
    );
  }

}
