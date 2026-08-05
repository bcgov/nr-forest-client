package ca.bc.gov.app.validator.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.service.client.ClientLegacyService;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DisplayName("Unit Tests | BusinessInformationBusinessTypeValidator")
class BusinessInformationBusinessLegalTypeValidatorTest {

  private final ClientLegacyService clientLegacyService = Mockito.mock(ClientLegacyService.class);
  private final BusinessInformationBusinessLegalTypeValidator validator = new BusinessInformationBusinessLegalTypeValidator(clientLegacyService);

  @BeforeEach
  void setUp() {
    when(clientLegacyService.findActiveRegistryTypeCodesByClientTypeCode("C"))
        .thenReturn(Flux.just(
            new CodeNameDto("A", "Extraprovincial Company"),
            new CodeNameDto("B", "BC Company"),
            new CodeNameDto("BC", "BC Company"),
            new CodeNameDto("C", "Continued In Corporation"),
            new CodeNameDto("EPR", "Extraprovincial Company"),
            new CodeNameDto("FOR", "Foreign Registration"),
            new CodeNameDto("LIC", "Licensed (Extra-Pro)"),
            new CodeNameDto("REG", "Extraprovincial Company"),
            new CodeNameDto("ULC", "Unlimited Liability Company")
        ));
    when(clientLegacyService.findActiveRegistryTypeCodesByClientTypeCode("A"))
        .thenReturn(Flux.just(
            new CodeNameDto("CP", "Cooperative"),
            new CodeNameDto("XCP", "Extraprovincial Cooperative")
        ));
    when(clientLegacyService.findActiveRegistryTypeCodesByClientTypeCode("S"))
        .thenReturn(Flux.just(
            new CodeNameDto("S", "Society"),
            new CodeNameDto("XS", "Extraprovincial Society")
        ));
  }

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
      String legalType,
      String businessType,
      String clientType,
      String expectedMessage
  ) {


    StepVerifier.FirstStep<ValidationError> validation =
        validator.validate(
                new ClientBusinessInformationDto(
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    businessType,
                    clientType,
                    StringUtils.EMPTY,
                    legalType,
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
              "businessInformation.legalType",
              expectedMessage
          )
      );
    }

    validation.verifyComplete();

  }

  private static Stream<Arguments> validation() {
    return
        Stream.of(
            Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, "Legal type has an invalid value []"),
            Arguments.of("J", StringUtils.EMPTY, StringUtils.EMPTY, "Legal type has an invalid value [J]"),
            Arguments.of("A", "U", StringUtils.EMPTY, StringUtils.EMPTY),
            Arguments.of("A", "R", "C", StringUtils.EMPTY),
            Arguments.of("CP", "R", "A", StringUtils.EMPTY),
            Arguments.of("CP", "R", "C", "C value for clientType does not match the expected value for legal type")
        );
  }
}