package ca.bc.gov.app.controller;

import ca.bc.gov.app.exception.MissingRequiredParameterException;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

@Slf4j
@DisplayName("Integrated Test | Client Search Controller")
class ClientSearchControllerIntegrationTest extends
    AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @ParameterizedTest
  @MethodSource("noResults")
  @DisplayName("Empty results for not found values")
  void shouldReturnEmpty(String name, String registrationNumber) {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/search/registrationOrName")
                .queryParam("registrationNumber", registrationNumber)
                .queryParam("companyName", name)
                .build(new HashMap<>())
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .json("[]");
  }

  @Test
  @DisplayName("Search someone by registration number")
  void shouldSearchByRegistrationNumber() {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/search/registrationOrName")
                .queryParam("registrationNumber", "00000001")
                .build(new HashMap<>())
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].clientNumber").isNotEmpty()
        .jsonPath("$[0].clientNumber").isEqualTo("00000001")
        .jsonPath("$[0].clientName").isNotEmpty()
        .jsonPath("$[0].clientName").isEqualTo("BAXTER")
        .jsonPath("$[0].legalFirstName").isNotEmpty()
        .jsonPath("$[0].legalFirstName").isEqualTo("JAMES")
        .jsonPath("$[0].legalMiddleName").isEmpty()
        .consumeWith(System.out::println);
  }

  @Test
  @DisplayName("We need at least one parameter")
  void shouldFailIfNoParamProvided() {
    client
        .get()
        .uri("/api/search/registrationOrName", new HashMap<>())
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isEqualTo(HttpStatusCode.valueOf(HttpStatus.EXPECTATION_FAILED.value()))
        .expectBody()
        .equals("Missing value for parameter registrationNumber or companyName");
  }

  @ParameterizedTest
  @MethodSource("byLastNameCompanyName")
  @DisplayName("Search someone by company name / last name")
  void shouldSearchByName(String name) {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/search/registrationOrName")
                .queryParam("companyName", name)
                .build(new HashMap<>())
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].clientNumber").isNotEmpty()
        .jsonPath("$[0].clientName").isNotEmpty()
        .jsonPath("$[0].clientName").isEqualTo(name)
        .consumeWith(System.out::println);
  }

  @Test
  @DisplayName("Search someone by individual data")
  void shouldSearchByIndividual() {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/search/individual")
                .queryParam("firstName", "JAMES")
                .queryParam("lastName", "BAXTER")
                .queryParam("dob", "1959-05-18")
                .build(new HashMap<>())
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].clientNumber").isNotEmpty()
        .jsonPath("$[0].clientName").isNotEmpty()
        .jsonPath("$[0].clientName").isEqualTo("BAXTER")
        .consumeWith(System.out::println);
  }

  @Test
  @DisplayName("Search someone by individual data with document")
  void shouldSearchByIndividualAndDocument() {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/search/individual")
                .queryParam("firstName", "THOMAS")
                .queryParam("lastName", "FUNNY")
                .queryParam("dob", "1939-07-04")
                .queryParam("identification", "34458787")
                .build(new HashMap<>())
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].clientNumber").isNotEmpty()
        .jsonPath("$[0].clientName").isNotEmpty()
        .jsonPath("$[0].clientName").isEqualTo("FUNNY")
        .consumeWith(System.out::println);
  }

  @ParameterizedTest
  @MethodSource("individuals")
  @DisplayName("Search someone by individual data and fail")
  void shouldSearchByIndividualFailures(
      String firstName,
      String lastName,
      LocalDate dob,
      String identification
  ) {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/search/individual")
                .queryParamIfPresent("firstName", Optional.ofNullable(firstName))
                .queryParamIfPresent("lastName", Optional.ofNullable(lastName))
                .queryParamIfPresent("dob", Optional.ofNullable(dob).map(value -> value.format(
                    DateTimeFormatter.ISO_DATE)))
                .queryParamIfPresent("identification", Optional.ofNullable(identification))
                .build(new HashMap<>())
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus()
        .is4xxClientError();
  }

  @ParameterizedTest
  @MethodSource("legalName")
  @DisplayName("Search someone by legal name")
  void shouldMatch(
      String searchParam,
      String expected
  ) {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/search/match")
                .queryParam("companyName", searchParam)
                .build(new HashMap<>())
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .json(expected)
        .consumeWith(System.out::println);
  }

  @ParameterizedTest
  @MethodSource("idLastName")
  @DisplayName("Search someone by id and last name")
  void shouldSearchByIdAndLastName(String clientId, String lastName) {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/search/idAndLastName")
                .queryParam("clientId", Optional.ofNullable(clientId))
                .queryParam("lastName", Optional.ofNullable(lastName))
                .build(new HashMap<>())
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].clientNumber").isNotEmpty()
        .jsonPath("$[0].clientNumber").isEqualTo("00000007")
        .jsonPath("$[0].clientName").isNotEmpty()
        .jsonPath("$[0].clientName").isEqualTo("bond")
        .consumeWith(System.out::println);
  }

  @ParameterizedTest
  @MethodSource("byDocument")
  @DisplayName("Search someone by document")
  void shouldSearchByDocument(
      String idType,
      String identification,
      String expected,
      Class<RuntimeException> exception
  ) {
    ResponseSpec response =
        client
            .get()
            .uri("/api/search/id/{idType}/{identification}", idType, identification)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .exchange();

    if (StringUtils.isNotBlank(expected)) {
      response
          .expectStatus().isOk()
          .expectBody()
          .jsonPath("$[0].clientNumber").isNotEmpty()
          .jsonPath("$[0].clientNumber").isEqualTo(expected)
          .jsonPath("$[0].clientName").isNotEmpty()
          .consumeWith(System.out::println);
    }

    if (exception != null) {
      response.expectStatus().is4xxClientError();
    }

  }

  private static Stream<String> byLastNameCompanyName() {
    return Stream.of("BAXTER", "BORIS AND BORIS INC.");
  }

  private static Stream<Arguments> noResults() {
    return
        Stream.of(
            Arguments.of("00000007", null),
            Arguments.of(null, "Jones Bar-B-Q")
        );
  }

  private static Stream<Arguments> individuals() {
    return Stream
        .of(
            Arguments.of("JAMES", "BAXTER", null, null),
            Arguments.of("JAMES", null, LocalDate.of(1959, 5, 18), null),
            Arguments.of(null, "BAXTER", LocalDate.of(1959, 5, 18), null),
            Arguments.of("JAMES", StringUtils.EMPTY, LocalDate.of(1959, 5, 18), null),
            Arguments.of(StringUtils.EMPTY, "BAXTER", LocalDate.of(1959, 5, 18), null)
        );
  }

  private static Stream<Arguments> legalName() {
    return
        Stream.of(
            Arguments.of("James", """
                [
                  {
                    "clientNumber":"00000009",
                    "clientName":"james",
                    "legalFirstName":null,
                    "legalMiddleName":"hunt",
                    "clientStatusCode":"ACT",
                    "clientTypeCode":"I",
                    "birthdate":null,
                    "clientIdTypeCode":null,
                    "clientIdentification":null,
                    "registryCompanyTypeCode":null,
                    "corpRegnNmbr":null
                  }
                ]"""),
            Arguments.of("Marco", "[]"),
            Arguments.of("Lucca", "[]")
        );
  }

  private static Stream<Arguments> idLastName() {
    return Stream
        .of(
            Arguments.of("Wull", "bond"),
            Arguments.of("wull", "BOND"),
            Arguments.of("wull", "BoNd")
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
