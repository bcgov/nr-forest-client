package ca.bc.gov.app.validator.business;

import static ca.bc.gov.app.util.ClientValidationUtils.fieldIsMissingErrorMessage;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Named.named;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryOfficerDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryPartyDto;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.service.bcregistry.BcRegistryService;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DisplayName("Unit Tests | BusinessInformationBusinessRegisteredNumberValidator")
class BusinessInformationBusinessRegisteredNumberValidatorTest {

  private final BcRegistryService service = mock(BcRegistryService.class);
  private final BusinessInformationBusinessRegisteredNumberValidator validator = new BusinessInformationBusinessRegisteredNumberValidator(
      service);


  @ParameterizedTest
  @MethodSource("ca.bc.gov.app.validator.address.AddressAddressValidatorTest#validSources")
  @DisplayName("Should support all validation sources")
  void shouldSupportAllValidationSources(ValidationSourceEnum source, boolean support) {
    assertEquals(support, validator.supports(source));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validation")
  @DisplayName("Should run validate")
  void shouldValidate(
      String registrationNumber,
      String legalType,
      String businessType,
      boolean isFoundOnBcRegistry,
      BcRegistryDocumentDto document,
      String expectedMessage
  ) {

    if (isFoundOnBcRegistry) {
      when(service.requestDocumentData(registrationNumber))
          .thenReturn(Flux.just(document));
    } else {
      when(service.requestDocumentData(registrationNumber))
          .thenReturn(Flux.empty());
    }

    StepVerifier.FirstStep<ValidationError> validation =
        validator.validate(
                new ClientBusinessInformationDto(
                    registrationNumber,
                    StringUtils.EMPTY,
                    businessType,
                    StringUtils.EMPTY,
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
                    StringUtils.EMPTY,
                    StringUtils.EMPTY
                ), 0
            )
            .as(StepVerifier::create);

    if (StringUtils.isNotBlank(expectedMessage)) {
      validation.expectNext(new ValidationError(
              "businessInformation.registrationNumber",
              expectedMessage
          )
      );
    }

    validation.verifyComplete();

  }

  private static Stream<Arguments> validation() {
    return
        Stream.of(
            Arguments.of(
                named("all fine, no issues", StringUtils.EMPTY),
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                false,
                null,
                StringUtils.EMPTY
            ),

            Arguments.of(
                named("Registration number missing", StringUtils.EMPTY),
                StringUtils.EMPTY,
                "R",
                false,
                null,
                fieldIsMissingErrorMessage("registrationNumber")
            ),

            Arguments.of(
                named("Big number, no no", "12345678901234"),
                StringUtils.EMPTY,
                "R",
                false,
                null,
                "has more than 13 characters"
            ),

            Arguments.of(
                named("This is what I call good", "XX1234567"),
                StringUtils.EMPTY,
                "R",
                false,
                null,
                StringUtils.EMPTY
            ),

            Arguments.of(named("Another good day around here", "XX1234567"),
                "C", "R", false, null,
                StringUtils.EMPTY),

            Arguments.of(named("Oops, not found on bc reg","XX1234567"),
                "SP", "R", false,null,
                "Incorporation Number was not found in BC Registry"),

            Arguments.of(named("I think you forgot to pay the bills","XX1234567"),
                "SP", "R", true,
                        new BcRegistryDocumentDto(
                            new BcRegistryBusinessDto(
                                List.of(),
                                false,
                                false,
                                false,
                                false,
                                "XX1234567",
                                "SAMPLE CORP",
                                StringUtils.EMPTY,
                                StringUtils.EMPTY
                            ),
                            null,
                            List.of(
                                new BcRegistryPartyDto(
                                    null,null,
                                    new BcRegistryOfficerDto(
                                        StringUtils.EMPTY,
                                        "Johnathan",
                                        "Wick",
                                        StringUtils.EMPTY,
                                        StringUtils.EMPTY,
                                        StringUtils.EMPTY,
                                        "person"
                                    ),
                                    List.of()
                                )
                            )
                        ),
                "Company is not in goodStanding in BC Registry"),

            Arguments.of(named("Sole prop good for belly","XX1234567"),
                "SP", "R", true,
                new BcRegistryDocumentDto(
                    new BcRegistryBusinessDto(
                        List.of(),
                        true,
                        false,
                        false,
                        false,
                        "XX1234567",
                        "SAMPLE CORP",
                        StringUtils.EMPTY,
                        StringUtils.EMPTY
                    ),
                    null,
                    List.of(
                        new BcRegistryPartyDto(
                            null,null,
                            new BcRegistryOfficerDto(
                                StringUtils.EMPTY,
                                "Johnathan",
                                "Wick",
                                StringUtils.EMPTY,
                                StringUtils.EMPTY,
                                StringUtils.EMPTY,
                                "person"
                            ),
                            List.of()
                        )
                    )
                ), StringUtils.EMPTY),

            Arguments.of(named("Sole prop from org is no bueno","XX1234567"),
                "SP", "R", true,
                new BcRegistryDocumentDto(
                    new BcRegistryBusinessDto(
                        List.of(),
                        true,
                        false,
                        false,
                        false,
                        "XX1234567",
                        "SAMPLE CORP",
                        StringUtils.EMPTY,
                        StringUtils.EMPTY
                    ),
                    null,
                    List.of(
                        new BcRegistryPartyDto(
                            null,null,
                            new BcRegistryOfficerDto(
                                StringUtils.EMPTY,
                                StringUtils.EMPTY,
                                StringUtils.EMPTY,
                                StringUtils.EMPTY,
                                StringUtils.EMPTY,
                                StringUtils.EMPTY,
                                "organization"
                            ),
                            List.of()
                        )
                    )
                ), "SAMPLE CORP sole proprietor is not owned by a person")
        );
  }

}