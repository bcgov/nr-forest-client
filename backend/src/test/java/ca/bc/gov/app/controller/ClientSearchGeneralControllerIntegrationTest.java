package ca.bc.gov.app.controller;

import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.util.HashMap;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;


@Slf4j
@DisplayName("Integrated Test | Client Search for Others Controller")
public class ClientSearchGeneralControllerIntegrationTest extends
    AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  void shouldSearchAcronym(
      String acronym,
      String response
  ) {

    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/search/acronym")
                .queryParam("acronym", Optional.ofNullable(acronym))
                .build(new HashMap<>())
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .json(response)
        .consumeWith(System.out::println);
  }

  void shouldSearchDoingBusinessAs() {

  }

  void shouldSearchClientName() {

  }

}
