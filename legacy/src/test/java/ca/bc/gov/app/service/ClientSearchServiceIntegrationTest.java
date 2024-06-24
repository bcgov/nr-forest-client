package ca.bc.gov.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.exception.MissingRequiredParameterException;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@DisplayName("Integrated Test | Client Search Service-")
class ClientSearchServiceIntegrationTest extends AbstractTestContainerIntegrationTest {

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
    StepVerifier.FirstStep<ForestClientDto> test =
        service
            .findByRegistrationNumberOrCompanyName(registrationNumber, companyName)
            .as(StepVerifier::create);

    verifyTestData(expected, exception, test);
  }

  @DisplayName("should find individual")
  @ParameterizedTest
  @MethodSource("byIndividual")
  void shouldFindIndividual(
      String firstName,
      String lastName,
      LocalDate dob,
      String identification,
      String expected,
      Class<RuntimeException> exception
  ) {
    StepVerifier.FirstStep<ForestClientDto> test =
        service
            .findByIndividual(firstName, lastName, dob, identification, true)
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
    StepVerifier.FirstStep<ForestClientDto> test =
        service
            .matchBy(companyName)
            .as(StepVerifier::create);

    verifyTestData(expected, exception, test);

  }

  @DisplayName("should find by id and last name")
  @ParameterizedTest
  @MethodSource("byIdLastName")
  void shouldFindByLastNameAndDocument(
      String lastName,
      String identification,
      String expected,
      Class<RuntimeException> exception
  ) {
    StepVerifier.FirstStep<ForestClientDto> test =
        service
            .findByIdAndLastName(identification, lastName)
            .as(StepVerifier::create);

    verifyTestData(expected, exception, test);

  }

  @DisplayName("should find by document")
  @ParameterizedTest
  @MethodSource("byDocument")
  void shouldFindByIdLas(
      String idType,
      String identification,
      String expected,
      Class<RuntimeException> exception
  ) {
    StepVerifier.FirstStep<ForestClientDto> test =
        service
            .findByIdentification(idType, identification)
            .as(StepVerifier::create);

    verifyTestData(expected, exception, test);

  }

  @DisplayName("should find by email on contact and location")
  @ParameterizedTest
  @MethodSource("byDocument")
  void shouldfindByGeneralEmail() {

  }

  @DisplayName("should find by email on contact and location")
  @Test
  void shouldFindByAddress() {
    StepVerifier.FirstStep<ForestClientDto> test =
    service
        .findByEntireAddress(
            "510 FULTON PLAZA",
            "FORT MCMURRAY",
            "AB",
            "T9J9R1",
            "CANADA"
        )
        .as(StepVerifier::create);

    verifyTestData("00000123", null, test);
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

  private static Stream<Arguments> byIndividual() {
    return Stream
        .of(
            Arguments.of("JAMES", "BAXTER", LocalDate.of(1959, 5, 18), null, "00000001", null),
            Arguments.of("THOMAS", "FUNNY", LocalDate.of(1939, 7, 4), "34458787", "00000002", null),
            Arguments.of("ALBUS", "DUMBLEDORE", LocalDate.of(1814, 5, 12), null, StringUtils.EMPTY,
                null),
            Arguments.of("JAMES", null, null, null, null, MissingRequiredParameterException.class),
            Arguments.of("JAMES", "Baxter", null, null, null,
                MissingRequiredParameterException.class),
            Arguments.of(null, "Baxter", LocalDate.of(1959, 5, 18), null, null,
                MissingRequiredParameterException.class),
            Arguments.of(null, null, LocalDate.of(1959, 5, 18), null, null,
                MissingRequiredParameterException.class)
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

  private static Stream<Arguments> byIdLastName() {
    return Stream
        .of(
            Arguments.of("FUNNY", "34458787", "00000002", null),
            Arguments.of("Baxter", null, null,
                MissingRequiredParameterException.class),
            Arguments.of(null, null, null,
                MissingRequiredParameterException.class)
        );
  }

  private static Stream<Arguments> byDocument() {
    return Stream
        .of(
            Arguments.of("BCDL", "9994457", "00000005", null),
            Arguments.of("BCSC", "Wull", "00000007", null),
            Arguments.of("BCDL", "3334447", StringUtils.EMPTY, null),
            Arguments.of("BCDL", null, null,
                MissingRequiredParameterException.class),
            Arguments.of(null, "9994457", null,
                MissingRequiredParameterException.class),
            Arguments.of(null, null, null,
                MissingRequiredParameterException.class)
        );
  }
}