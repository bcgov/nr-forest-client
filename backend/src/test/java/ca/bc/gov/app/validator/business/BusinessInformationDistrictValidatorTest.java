package ca.bc.gov.app.validator.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.entity.client.DistrictCodeEntity;
import ca.bc.gov.app.repository.client.DistrictCodeRepository;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Unit Tests | BusinessInformationDistrictValidator")
class BusinessInformationDistrictValidatorTest {

  private final DistrictCodeRepository districtCodeRepository = mock(DistrictCodeRepository.class);
  private final BusinessInformationDistrictValidator validator = new BusinessInformationDistrictValidator(
      districtCodeRepository);

  @ParameterizedTest
  @MethodSource("ca.bc.gov.app.validator.address.AddressAddressValidatorTest#externalSource")
  @DisplayName("Should support all validation sources")
  void shouldSupportAllValidationSources(ValidationSourceEnum source, boolean support) {
    assertEquals(support, validator.supports(source));
  }


  @ParameterizedTest
  @MethodSource("validation")
  @DisplayName("Should run validate")
  void shouldValidate(
      String district,
      boolean isDbFound,
      String expectedMessage
  ) {

    if (isDbFound) {
      when(districtCodeRepository.findByCode(district))
          .thenReturn(
              Mono.just(
                  DistrictCodeEntity
                      .builder()
                      .code(district)
                      .description(district)
                      .build()
              )
          );
    } else {
      when(districtCodeRepository.findByCode(district))
          .thenReturn(Mono.empty());
    }

    StepVerifier.FirstStep<ValidationError> validation =
        validator.validate(
                new ClientBusinessInformationDto(
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    null,
                    district,
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
              "businessInformation.district",
              expectedMessage
          )
      );
    }

    validation.verifyComplete();

  }

  private static Stream<Arguments> validation() {
    return
        Stream.of(
            Arguments.of(StringUtils.EMPTY, false, "Client does not have a district"),
            Arguments.of("XXX", false, "district is invalid"),
            Arguments.of("DKM", true, StringUtils.EMPTY)
        );
  }

}