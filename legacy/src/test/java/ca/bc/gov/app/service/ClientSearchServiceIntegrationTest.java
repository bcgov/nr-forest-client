package ca.bc.gov.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ca.bc.gov.app.dto.AddressSearchDto;
import ca.bc.gov.app.dto.ContactSearchDto;
import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.dto.PredictiveSearchResultDto;
import ca.bc.gov.app.exception.MissingRequiredParameterException;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@DisplayName("Integrated Test | Client Search Service")
class ClientSearchServiceIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  private ClientSearchService service;

  @DisplayName("should find by email on contact and location")
  @ParameterizedTest
  @MethodSource("byEmail")
  void shouldFindByGeneralEmail(
      String email,
      List<String> expected,
      Class<RuntimeException> exception
  ) {

    FirstStep<ForestClientDto> test =
        service
            .findByGeneralEmail(email)
            .as(StepVerifier::create);

    verifyTestData(expected, exception, test);

  }

  @DisplayName("should find by phone on contact and location")
  @ParameterizedTest
  @MethodSource("byPhone")
  void shouldFindByGeneralPhone(
      String phone,
      List<String> expected,
      Class<RuntimeException> exception
  ) {

    FirstStep<ForestClientDto> test =
        service
            .findByGeneralPhoneNumber(phone)
            .as(StepVerifier::create);

    verifyTestData(expected, exception, test);

  }

  @DisplayName("should find by location")
  @ParameterizedTest
  @MethodSource("byLocation")
  void shouldFindByLocation(
      AddressSearchDto location,
      String expected,
      Class<RuntimeException> exception
  ) {

    FirstStep<ForestClientDto> test =
        service
            .findByEntireAddress(location)
            .as(StepVerifier::create);

    verifyTestData(expected, exception, test);

  }

  @DisplayName("should find by contact")
  @ParameterizedTest
  @MethodSource("byContact")
  void shouldFindByContact(
      ContactSearchDto contact,
      String expected,
      Class<RuntimeException> exception
  ) {

    FirstStep<ForestClientDto> test =
        service
            .findByContact(contact)
            .as(StepVerifier::create);

    verifyTestData(expected, exception, test);

  }

  @DisplayName("should do predictive search")
  @ParameterizedTest
  @MethodSource("byPredictive")
  void shouldSearchWithComplexSearch(
      String searchValue,
      String expectedClientNumber,
      String expectedClientName
  ) {

    FirstStep<Pair<PredictiveSearchResultDto,Long>> test =
        service
            .complexSearch(searchValue, PageRequest.of(0, 5))
            .as(StepVerifier::create);

    if (StringUtils.isNotBlank(expectedClientNumber)) {
      test
          .assertNext(dto -> {
            assertNotNull(dto);
            assertEquals(expectedClientNumber, dto.getKey().clientNumber());
            assertEquals(expectedClientName, dto.getKey().clientFullName());
            assertNotNull(dto.getValue());
            assertEquals(1, dto.getValue());
          });
    }
          test.verifyComplete();
  }

  private void verifyTestData(
      List<String> expectedList,
      Class<RuntimeException> exception,
      FirstStep<ForestClientDto> test
  ) {
    if (expectedList != null && !expectedList.isEmpty()) {
      for(String expected: expectedList) {
        if (StringUtils.isNotBlank(expected)) {
          test
              .assertNext(dto -> {
                assertNotNull(dto);
                assertEquals(expected, dto.clientNumber());
              });
        }
      }
    }


    if (exception != null) {
      test.expectError(exception);
    }

    test.verifyComplete();
  }

  private void verifyTestData(
      String expected,
      Class<RuntimeException> exception,
      FirstStep<ForestClientDto> test
  ) {
    verifyTestData(
        expected == null ? List.of() : List.of(expected),
        exception,
        test
    );
  }

  private static Stream<Arguments> byEmail() {
    return
        Stream.concat(
            emptyCases(),
            Stream
                .of(
                    Arguments.of("celinedion@email.ca", List.of(), null),
                    Arguments.of("uturfes0@cnn.com", List.of("00000103"), null),
                    Arguments.of("themail@mail.ca", List.of("00000006"), null)
                )
        );
  }

  private static Stream<Arguments> byPhone() {
    return
        Stream.concat(
            emptyCases(), Stream
                .of(
                    Arguments.of("1232504567", List.of(), null),
                    Arguments.of("2502502550", List.of("00000004"), null),
                    Arguments.of("2894837433", List.of("00000103"), null)
                )
        );
  }

  private static Stream<Arguments> byLocation() {
    return Stream
        .of(
            Arguments.of(null, null, MissingRequiredParameterException.class),
            Arguments.of(new AddressSearchDto(
                "",
                "",
                "",
                "",
                ""
            ), null, MissingRequiredParameterException.class),
            Arguments.of(new AddressSearchDto(
                "510 FULTON PLAZA",
                "FORT MCMURRAY",
                "AB",
                "T9J9R1",
                ""
            ), null, MissingRequiredParameterException.class),
            Arguments.of(new AddressSearchDto(
                "510 FULTON PLAZA",
                "FORT MCMURRAY",
                "AB",
                "T9J9R1",
                "CANADA"
            ), "00000123", null)
        );
  }

  private static Stream<Arguments> byContact() {
    return Stream
        .of(
            Arguments.of(new ContactSearchDto(
                "",
                "",
                "",
                "",
                "",
                "",
                ""
            ), null, MissingRequiredParameterException.class),
            Arguments.of(new ContactSearchDto(
                "RICARDO",
                "JAMESON",
                "",
                "RBRISLEN5@UN.ORG",
                "7589636074",
                "8598150782",
                "8225477595"
            ), null, MissingRequiredParameterException.class),
            Arguments.of(new ContactSearchDto(
                "RICARDO",
                "JAMESON",
                "BRISLEN",
                "",
                "7589636074",
                "8598150782",
                "8225477595"
            ), null, null),
            Arguments.of(new ContactSearchDto(
                "RICARDO",
                "JAMESON",
                "BRISLEN",
                "RBRISLEN5@UN.ORG",
                "",
                "",
                ""
            ), null, null),
            Arguments.of(null, null, MissingRequiredParameterException.class),
            Arguments.of(new ContactSearchDto(
                "RICARDO",
                "",
                "BRISLEN",
                "RBRISLEN5@UN.ORG",
                "7589636074",
                "8598150782",
                "8225477595"
            ), "00000137", null),
            Arguments.of(new ContactSearchDto(
                "RICARDO",
                "",
                "BRIEN",
                "RBRISLEN5@UN.ORG",
                "7589636074",
                "8598150782",
                "8225477595"
            ), "00000137", null),
            Arguments.of(new ContactSearchDto(
                "RICARDO",
                null,
                "BRISLEN",
                "RBRISLEN5@UN.ORG",
                "7589636074",
                "8598150782",
                "8225477595"
            ), "00000137", null),
            Arguments.of(new ContactSearchDto(
                "RICARDO",
                "  ",
                "BRISLEN",
                "RBRISLEN5@UN.ORG",
                "7589636074",
                "8598150782",
                "8225477595"
            ), "00000137", null),
            Arguments.of(new ContactSearchDto(
                "RICARDO",
                "JAMESON",
                "BRISLEN",
                "RBRISLEN5@UN.ORG",
                "7589636074",
                "8598150782",
                "8225477595"
            ), null, null),
            Arguments.of(new ContactSearchDto(
                "RANDOLPH",
                null,
                "BRISLEN",
                "RBRISLEN5@UN.ORG",
                "7589636074",
                "8598150782",
                "8225477595"
            ), null, null)
        );
  }

  private static Stream<Arguments> emptyCases() {
    return Stream
        .of(
            Arguments.of(null, null, MissingRequiredParameterException.class),
            Arguments.of(StringUtils.EMPTY, null, MissingRequiredParameterException.class),
            Arguments.of("  ", null, MissingRequiredParameterException.class)
        );
  }

  private static Stream<Arguments> byPredictive() {
    return Stream
        .of(
            Arguments.of("pollich", "00000114", "POLLICH-ABERNATHY"),
            Arguments.of("kilback", "00000123", "REICHERT, KILBACK AND EMARD"),
            Arguments.of("darbie", "00000145", "DARBIE BLIND (MINYX)"),
            Arguments.of("pietro", StringUtils.EMPTY, StringUtils.EMPTY)
        );
  }


}