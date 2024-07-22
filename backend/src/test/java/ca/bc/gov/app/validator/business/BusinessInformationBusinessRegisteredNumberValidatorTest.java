package ca.bc.gov.app.validator.business;

import static ca.bc.gov.app.util.ClientValidationUtils.fieldIsMissingErrorMessage;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.bcregistry.BcRegistryBusinessDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentDto;
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
import reactor.core.publisher.Mono;
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

  @ParameterizedTest
  @MethodSource("validation")
  @DisplayName("Should run validate")
  void shouldValidate(
      String registrationNumber,
      String legalType,
      String businessType,
      boolean isFoundOnBcRegistry,
      boolean isGoodStanding,
      String expectedMessage
  ) {

    if (isFoundOnBcRegistry) {
      when(service.requestDocumentData(registrationNumber))
          .thenReturn(Flux.just(
                  new BcRegistryDocumentDto(
                      new BcRegistryBusinessDto(
                          isGoodStanding,
                          false,
                          false,
                          false,
                          registrationNumber,
                          StringUtils.EMPTY,
                          StringUtils.EMPTY,
                          StringUtils.EMPTY
                      ),
                      null,
                      List.of()
                  )
              )
          );
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
            Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, false, false,
                StringUtils.EMPTY),
            Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY, "R", false, false,
                fieldIsMissingErrorMessage("registrationNumber")),
            Arguments.of("12345678901234", StringUtils.EMPTY, "R", false, false,
                "has more than 13 characters"),
            Arguments.of("XX1234567", StringUtils.EMPTY, "R", false, false, StringUtils.EMPTY),
            Arguments.of("XX1234567", "C", "R", false, false, StringUtils.EMPTY),
            Arguments.of("XX1234567", "SP", "R", false, false,
                "Incorporation Number was not found in BC Registry"),
            Arguments.of("XX1234567", "SP", "R", true, false,
                "Company is not in goodStanding in BC Registry"),
            Arguments.of("XX1234567", "SP", "R", true, true, StringUtils.EMPTY)
        );
  }

}