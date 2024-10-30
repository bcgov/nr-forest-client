package ca.bc.gov.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.exception.MissingRequiredParameterException;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@DisplayName("Integrated Test | Client Search for Company Service-")
class ClientSearchCompanyServiceIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  private ClientSearchService service;

  @DisplayName("should find by registration number or company name")
  @ParameterizedTest
  @MethodSource("byNumberOrCompanyName")
  void shouldFindByRegistrationNumberOrCompanyName(
      String registrationNumber,
      String companyName,
      String expected,
      Class<RuntimeException> exception
  ) {
    FirstStep<ForestClientDto> test =
        service
            .findByRegistrationNumberOrCompanyName(registrationNumber, companyName)
            .as(StepVerifier::create);

    verifyTestData(expected, exception, test);
  }

  @DisplayName("should find by match")
  @ParameterizedTest
  @MethodSource("byMatch")
  void shouldFindByMatch(
      String companyName,
      String expected,
      Class<RuntimeException> exception
  ) {
    FirstStep<ForestClientDto> test =
        service
            .matchBy(companyName)
            .as(StepVerifier::create);

    verifyTestData(expected, exception, test);

  }

  private void verifyTestData(
      String expected,
      Class<RuntimeException> exception,
      FirstStep<ForestClientDto> test
  ) {
    if (StringUtils.isNotBlank(expected)) {
      test
          .assertNext(dto -> {
            assertNotNull(dto);
            assertEquals(expected, dto.clientNumber());
          });
    }

    if (exception != null) {
      test.expectError(exception);
    }

    test.verifyComplete();
  }

  private static Stream<Arguments> byNumberOrCompanyName() {
    return Stream.of(
        Arguments.of("BC123444789", null, "00000011", null),
        Arguments.of(null, "INDIAN CANADA", "00000006", null),
        Arguments.of(null, "ELARICHO", "00000005", null),
        Arguments.of("AB994455454", null, StringUtils.EMPTY, null),
        Arguments.of(null, "Zamasu Corp", StringUtils.EMPTY, null),
        Arguments.of(null, null, null, MissingRequiredParameterException.class)
    );
  }

  private static Stream<Arguments> byMatch() {
    return Stream
        .of(
            Arguments.of("BAXTER", "00000001", null),
            Arguments.of("SAMPLE INDIAN BAND COUNC", "00000004", null),
            Arguments.of("FALCONI CORP", StringUtils.EMPTY, null),
            Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY,
                MissingRequiredParameterException.class),
            Arguments.of(null, StringUtils.EMPTY, MissingRequiredParameterException.class)
        );
  }
}