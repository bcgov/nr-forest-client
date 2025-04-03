package ca.bc.gov.app.validator.business;

import static org.junit.jupiter.api.Assertions.*;

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

@DisplayName("Unit Tests | BusinessInformationIdentificationDocumentValidator")
class BusinessInformationIdentificationDocumentValidatorTest {

  private final BusinessInformationIdentificationDocumentValidator validator = new BusinessInformationIdentificationDocumentValidator();

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
    return
        Stream.of(
           Arguments.of(StringUtils.EMPTY,"CA","BC","123456789","identificationType","You must select an identification type"),
            Arguments.of("CCTV",StringUtils.EMPTY,StringUtils.EMPTY,StringUtils.EMPTY,"identificationType","You must select an identification type"),
            Arguments.of("CDDL","CA","BC",StringUtils.EMPTY,"clientIdentification","You must enter an identification number"),

            Arguments.of("BRTH",StringUtils.EMPTY,StringUtils.EMPTY,"1","clientIdentification","The birth certificate must be a 12 or 13-digit number"),
            Arguments.of("BRTH",StringUtils.EMPTY,StringUtils.EMPTY,"12345678901234","clientIdentification","The birth certificate must be a 12 or 13-digit number"),
            Arguments.of("BRTH",StringUtils.EMPTY,StringUtils.EMPTY,"potato","clientIdentification","The birth certificate must be a 12 or 13-digit number"),
            Arguments.of("BRTH",StringUtils.EMPTY,StringUtils.EMPTY,"123456789012",StringUtils.EMPTY,StringUtils.EMPTY),

            Arguments.of("CDDL","CA","BC","123456789","clientIdentification","The driver's license must be a 7 or 8-digit number"),
            Arguments.of("CDDL","CA","BC","123","clientIdentification","The driver's license must be a 7 or 8-digit number"),
            Arguments.of("CDDL","CA","BC","potato","clientIdentification","The driver's license must be a 7 or 8-digit number"),
            Arguments.of("CDDL","CA","BC","12345678",StringUtils.EMPTY,StringUtils.EMPTY),

            Arguments.of("PASS",StringUtils.EMPTY,StringUtils.EMPTY,"!A1","clientIdentification","The passport must be 8 characters and contain only letters and numbers"),
            Arguments.of("PASS",StringUtils.EMPTY,StringUtils.EMPTY,"12345678901234","clientIdentification","The passport must be 8 characters and contain only letters and numbers"),
            Arguments.of("PASS",StringUtils.EMPTY,StringUtils.EMPTY,"potato","clientIdentification","The passport must be 8 characters and contain only letters and numbers"),
            Arguments.of("PASS",StringUtils.EMPTY,StringUtils.EMPTY,"XX123456",StringUtils.EMPTY,StringUtils.EMPTY),

            Arguments.of("CITZ",StringUtils.EMPTY,StringUtils.EMPTY,"A1","clientIdentification","The Canadian Citizenship must be a 8-digit number"),
            Arguments.of("CITZ",StringUtils.EMPTY,StringUtils.EMPTY,"12345678901234","clientIdentification","The Canadian Citizenship must be a 8-digit number"),
            Arguments.of("CITZ",StringUtils.EMPTY,StringUtils.EMPTY,"1","clientIdentification","The Canadian Citizenship must be a 8-digit number"),
            Arguments.of("CITZ",StringUtils.EMPTY,StringUtils.EMPTY,"12345678",StringUtils.EMPTY,StringUtils.EMPTY),

            Arguments.of("FNID",StringUtils.EMPTY,StringUtils.EMPTY,"A1","clientIdentification","The First Nations ID must be a 10-digit number"),
            Arguments.of("FNID",StringUtils.EMPTY,StringUtils.EMPTY,"12345678901234","clientIdentification","The First Nations ID must be a 10-digit number"),
            Arguments.of("FNID",StringUtils.EMPTY,StringUtils.EMPTY,"1","clientIdentification","The First Nations ID must be a 10-digit number"),
            Arguments.of("FNID",StringUtils.EMPTY,StringUtils.EMPTY,"1234567890",StringUtils.EMPTY,StringUtils.EMPTY),

            Arguments.of("OTHR",StringUtils.EMPTY,StringUtils.EMPTY,"A1","clientIdentification","The other document must be between 3 and 40 characters long"),
            Arguments.of("OTHR",StringUtils.EMPTY,StringUtils.EMPTY,"A1".repeat(22),"clientIdentification","The other document must be between 3 and 40 characters long"),
            Arguments.of("OTHR",StringUtils.EMPTY,StringUtils.EMPTY,"12345","clientIdentification","Other identification must follow the pattern: [ID Type] : [ID Value] such as 'USA Passport : 12345'"),
            Arguments.of("OTHR",StringUtils.EMPTY,StringUtils.EMPTY,"USA Passport : 1","clientIdentification","Other identification must follow the pattern: [ID Type] : [ID Value] such as 'USA Passport : 12345'"),
            Arguments.of("OTHR",StringUtils.EMPTY,StringUtils.EMPTY,"USA Passport ; 12345","clientIdentification","Other identification must follow the pattern: [ID Type] : [ID Value] such as 'USA Passport : 12345'"),
            Arguments.of("OTHR",StringUtils.EMPTY,StringUtils.EMPTY,"A : 1","clientIdentification","Other identification must follow the pattern: [ID Type] : [ID Value] such as 'USA Passport : 12345'"),
            Arguments.of("OTHR",StringUtils.EMPTY,StringUtils.EMPTY,"USA Passport : 12345",StringUtils.EMPTY,StringUtils.EMPTY),

            Arguments.of("CDDL","CA","AB","1".repeat(22),"clientIdentification","The driver's license must be between 7 and 20 characters and contain only letters and numbers"),
            Arguments.of("CDDL","CA","NL","123","clientIdentification","The driver's license must be between 7 and 20 characters and contain only letters and numbers"),
            Arguments.of("CDDL","CA","ON","!A1","clientIdentification","The driver's license must be between 7 and 20 characters and contain only letters and numbers"),
            Arguments.of("CDDL","CA","SK","AB12345678",StringUtils.EMPTY,StringUtils.EMPTY),

            Arguments.of("USDL","US","CA","1".repeat(22),"clientIdentification","The driver's license must be between 7 and 20 characters and contain only letters and numbers"),
            Arguments.of("USDL","US","CA","123","clientIdentification","The driver's license must be between 7 and 20 characters and contain only letters and numbers"),
            Arguments.of("USDL","US","CA","!A1","clientIdentification","The driver's license must be between 7 and 20 characters and contain only letters and numbers"),
            Arguments.of("USDL","US","CA","CA12345678",StringUtils.EMPTY,StringUtils.EMPTY)
        );
  }

}