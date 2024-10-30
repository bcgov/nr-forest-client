package ca.bc.gov.app.validator.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.dto.ValidationError;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.client.ValidationSourceEnum;
import ca.bc.gov.app.entity.client.CountryCodeEntity;
import ca.bc.gov.app.repository.client.CountryCodeRepository;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Unit Tests | AddressCountryValidator")
class AddressCountryValidatorTest {

  private final CountryCodeRepository countryCodeRepository = mock(CountryCodeRepository.class);
  private final AddressCountryValidator validator = new AddressCountryValidator(
      countryCodeRepository);

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
      String countryCode,
      String dbCountryCode,
      String expectedMessage
  ) {

    if (StringUtils.isNotBlank(dbCountryCode)) {
      when(countryCodeRepository.findByCountryCode(countryCode))
          .thenReturn(
              Mono.just(
                  CountryCodeEntity
                      .builder()
                      .countryCode(dbCountryCode)
                      .description(dbCountryCode)
                      .build()
              )
          );
    } else {
      when(countryCodeRepository.findByCountryCode(eq(countryCode)))
          .thenReturn(Mono.empty());
    }

    StepVerifier.FirstStep<ValidationError> validation =
        validator.validate(
                new ClientAddressDto(
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    new ClientValueTextDto(countryCode, StringUtils.EMPTY),
                    null,
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
              "location.addresses[0].country",
              expectedMessage
          )
      );
    }

    validation.verifyComplete();

  }

  private static Stream<Arguments> validation() {
    return
        Stream.of(
            Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY,
                "You must select a country from the list."),
            Arguments.of("XX", StringUtils.EMPTY, "Country is invalid"),
            Arguments.of("CA", "CA", StringUtils.EMPTY)
        );
  }

}