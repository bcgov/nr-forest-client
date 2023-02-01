package ca.bc.gov.app.endpoints.client;

import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@DisplayName("Integrated Test | FSA Client Controller")
class ClientHandlerIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @Test
  @DisplayName("Codes are in expected order")
  void shouldListCodesAsExpected() {

    client
        .get()
        .uri("/api/clients/activeClientTypeCodes")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].code").isNotEmpty()
        .jsonPath("$[0].code").isEqualTo("A")

        .jsonPath("$[11].code").isNotEmpty()
        .jsonPath("$[11].code").isEqualTo("U");

  }


}
