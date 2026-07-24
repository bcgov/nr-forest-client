package ca.bc.gov.app.validator.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.service.client.ClientLegacyService;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DisplayName("Unit Tests | BusinessInformationClientTypeCompanyXrefValidator")
class BusinessInformationClientTypeCompanyXrefValidatorTest {

  private final ClientLegacyService clientLegacyService = mock(ClientLegacyService.class);
  private final BusinessInformationClientTypeCompanyXrefValidator validator =
      new BusinessInformationClientTypeCompanyXrefValidator(clientLegacyService);

  @ParameterizedTest
  @MethodSource("ca.bc.gov.app.validator.address.AddressAddressValidatorTest#validSources")
  @DisplayName("Should support all validation sources")
  void shouldSupportAllValidationSources(ValidationSourceEnum source, boolean support) {
    assertEquals(support, validator.supports(source));
  }

  @ParameterizedTest
  @MethodSource("validation")
  @DisplayName("Should validate clientType + legalType combination against CLIENT_TYPE_COMPANY_XREF")
  void shouldValidateCombination(
      String clientType,
      String legalType,
      String[] validRegistryTypes,
      String expectedMessage
  ) {

    if (validRegistryTypes != null) {
      Flux<CodeNameDto> registryTypesFlux = Flux.fromArray(validRegistryTypes)
          .map(code -> new CodeNameDto(code, "Type " + code));
      when(clientLegacyService.findActiveRegistryTypeCodesByClientTypeCode(clientType))
          .thenReturn(registryTypesFlux);
    } else {
      when(clientLegacyService.findActiveRegistryTypeCodesByClientTypeCode(clientType))
          .thenReturn(Flux.empty());
    }

    StepVerifier.FirstStep<ValidationError> step = validator.validate(
        new ClientBusinessInformationDto(
            StringUtils.EMPTY,       // registrationNumber
            StringUtils.EMPTY,       // businessName
            StringUtils.EMPTY,       // businessType
            clientType,              // clientType
            StringUtils.EMPTY,       // goodStandingInd
            legalType,               // legalType
            null,                    // birthdate
            StringUtils.EMPTY,       // district
            StringUtils.EMPTY,       // workSafeBcNumber
            StringUtils.EMPTY,       // doingBusinessAs
            StringUtils.EMPTY,       // clientAcronym
            StringUtils.EMPTY,       // firstName
            StringUtils.EMPTY,       // middleName
            StringUtils.EMPTY,       // lastName
            StringUtils.EMPTY,       // notes
            null,                    // identificationType
            StringUtils.EMPTY,       // clientIdentification
            StringUtils.EMPTY,       // identificationCountry
            StringUtils.EMPTY        // identificationProvince
        ),
        0
    ).as(StepVerifier::create);

    if (StringUtils.isNotBlank(expectedMessage)) {
      step.expectNext(new ValidationError(
          "businessInformation.legalType",
          expectedMessage
      ));
    }

    step.verifyComplete();
  }

  private static Stream<Arguments> validation() {
    return Stream.of(
        // Blank clientType - should pass (empty validation)
        Arguments.of(StringUtils.EMPTY, "C", new String[]{"C"}, StringUtils.EMPTY),

        // Null legalType - should pass (empty validation)
        Arguments.of("C", null, new String[]{"C"}, StringUtils.EMPTY),

        // Invalid legalType for clientType - combination not in xref
        Arguments.of(
            "C", "SP",
            new String[]{"A", "B", "BC", "EPR", "FOR", "LIC", "REG", "C"},
            "Legal type SP is not valid for client type C"
        ),

        // Valid legalType for clientType - combination exists in xref
        Arguments.of("C", "BC", new String[]{"A", "B", "BC", "EPR", "FOR", "LIC", "REG", "C"},
            StringUtils.EMPTY),

        // Valid SP for clientType RSP - combination exists in xref
        Arguments.of("RSP", "SP", new String[]{"SP"},
            StringUtils.EMPTY),

        // Invalid legalType for clientType I - FM is valid, BC is not
        Arguments.of("I", "BC", new String[]{"FM"},
            "Legal type BC is not valid for client type I"),

        // Valid FM for clientType I
        Arguments.of("I", "FM", new String[]{"FM"},
            StringUtils.EMPTY),

        // Empty valid registry types - should fail
        Arguments.of("C", "BC", new String[]{},
            "Legal type BC is not valid for client type C"),

        // No valid registry types returned from legacy - should fail
        Arguments.of("X", "SP", null,
            "Legal type SP is not valid for client type X")
    );
  }
}
