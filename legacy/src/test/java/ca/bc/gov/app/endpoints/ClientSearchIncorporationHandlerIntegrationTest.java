package ca.bc.gov.app.endpoints;

import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.util.HashMap;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@DisplayName("Integrated Test | Client Search Incorporation Controller")
class ClientSearchIncorporationHandlerIntegrationTest extends
    AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @ParameterizedTest
  @MethodSource("noResults")
  @DisplayName("Empty results for not found values")
  void shouldReturnEmpty(String name, String incorporationNumber) {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/search/incorporationOrName")
                .queryParam("incorporationNumber", incorporationNumber)
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
  @DisplayName("Search someone by incorporation number")
  void shouldSearchByIncorporationNumber() {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/search/incorporationOrName")
                .queryParam("incorporationNumber", "00000001")
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
        .uri("/api/search/incorporationOrName", new HashMap<>())
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isEqualTo(HttpStatusCode.valueOf(HttpStatus.EXPECTATION_FAILED.value()))
        .expectBody()
        .equals("Missing value for parameter incorporationNumber or companyName");
  }

  @ParameterizedTest
  @MethodSource("byLastNameCompanyName")
  @DisplayName("Search someone by company name / last name")
  void shouldSearchByName(String name) {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/search/incorporationOrName")
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

}
