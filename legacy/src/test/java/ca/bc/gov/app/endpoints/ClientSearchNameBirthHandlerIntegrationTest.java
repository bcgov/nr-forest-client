package ca.bc.gov.app.endpoints;

import ca.bc.gov.app.ApplicationConstants;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.util.HashMap;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@DisplayName("Integrated Test | Client Search Name and Birth Controller")
class ClientSearchNameBirthHandlerIntegrationTest extends
    AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;


  @Test
  @DisplayName("Search someone by name and birth")
  void shouldSearchSomeoneByNameAndBirth() {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/search/nameAndBirth")
                .queryParam(ApplicationConstants.CLIENT_SEARCH_FIRST_NAME, "JAMES")
                .queryParam(ApplicationConstants.CLIENT_SEARCH_LAST_NAME, "BAXTER")
                .queryParam(ApplicationConstants.CLIENT_SEARCH_BIRTHDATE, "1959-05-18")
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
  @DisplayName("Search company by name")
  void shouldSearchCompanyByName() {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/search/nameAndBirth")
                .queryParam(ApplicationConstants.CLIENT_SEARCH_LAST_NAME, "BORIS AND BORIS INC.")
                .queryParam(ApplicationConstants.CLIENT_SEARCH_FIRST_NAME, Optional.empty())
                .queryParam(ApplicationConstants.CLIENT_SEARCH_BIRTHDATE, Optional.empty())
                .build(new HashMap<>())
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].clientNumber").isNotEmpty()
        .jsonPath("$[0].clientNumber").isEqualTo("00000003")
        .jsonPath("$[0].clientName").isNotEmpty()
        .jsonPath("$[0].clientName").isEqualTo("BORIS AND BORIS INC.")
        .jsonPath("$[0].legalFirstName").isEmpty()
        .jsonPath("$[0].legalMiddleName").isEmpty()
        .consumeWith(System.out::println);
  }

  @Test
  @DisplayName("Fail When parameter is missing")
  void shouldFailWhenMissingParam() {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/search/nameAndBirth")
                .queryParam(ApplicationConstants.CLIENT_SEARCH_FIRST_NAME, "BORIS AND BORIS INC.")
                .queryParam(ApplicationConstants.CLIENT_SEARCH_LAST_NAME, Optional.empty())
                .build(new HashMap<>())
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.EXPECTATION_FAILED.value())
        .expectBody()
        .equals("Missing value for parameter "+ApplicationConstants.CLIENT_SEARCH_BIRTHDATE);
  }

}
