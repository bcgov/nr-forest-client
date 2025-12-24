package ca.bc.gov.app.controller;

import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.util.HashMap;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;

@Slf4j
@DisplayName("Integrated Test | Client Search for Companies Controller")
class ClientSearchCompanyControllerIntegrationTest extends
    AbstractTestContainerIntegrationTest {

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
                .queryParam("registrationNumber", "FM00000001")
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

}
