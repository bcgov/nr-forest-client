package ca.bc.gov.app.validator.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.entity.client.ProvinceCodeEntity;
import ca.bc.gov.app.repository.client.ProvinceCodeRepository;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Unit Tests | AddressProvinceValidator")
class AddressProvinceValidatorTest {

  private final ProvinceCodeRepository repository = mock(ProvinceCodeRepository.class);
  private final AddressProvinceValidator validator = new AddressProvinceValidator(repository);

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
      String provinceCode,
      String countryCode,
      boolean hasInDb,
      String expectedMessage
  ) {

    if (hasInDb) {
      when(repository.findByCountryCodeAndProvinceCode(countryCode, provinceCode))
          .thenReturn(Mono.justOrEmpty(ProvinceCodeEntity.builder().build()));
    } else {
      when(repository.findByCountryCodeAndProvinceCode(countryCode, provinceCode))
          .thenReturn(Mono.empty());
    }

    StepVerifier.FirstStep<ValidationError> validation =
        validator.validate(
                new ClientAddressDto(
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.isBlank(countryCode) ? null
                        : new ClientValueTextDto(countryCode, StringUtils.EMPTY),
                    new ClientValueTextDto(provinceCode, StringUtils.EMPTY),
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
              "location.addresses[0].province",
              expectedMessage
          )
      );
    }

    validation.verifyComplete();

  }

  private static Stream<Arguments> validation() {
    return
        Stream.of(
            Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY, false,
                "You must select a province, territory or state."),
            Arguments.of(StringUtils.EMPTY, "CA", false,
                "You must select a province or territory."),
            Arguments.of(StringUtils.EMPTY, "US", false, "You must select a state."),
            Arguments.of("BC", StringUtils.EMPTY, true,
                "province, territory or state doesn't belong to country"),
            Arguments.of("BC", "CA", true, StringUtils.EMPTY),
            Arguments.of("SP", "CA", false, "province or territory doesn't belong to country")
        );
  }

}