package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.ClientDoingBusinessAsDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.util.Map;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

@Slf4j
@DisplayName("Integrated Test | Client Doing Business As Controller")
class ClientDoingBusinessAsControllerIntegrationTest extends
    AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @ParameterizedTest
  @MethodSource("saveDba")
  @DisplayName("Save a location")
  void shouldSaveLocation(String clientNumber) {
    client
        .post()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/dba")
                .build(Map.of())
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .body(BodyInserters.fromValue(
                new ClientDoingBusinessAsDto(
                    clientNumber,
                    "Elaricho",
                    "TEST",
                    "TEST",
                    1L
                )
            )
        )
        .exchange()
        .expectStatus().isCreated()
        .expectBody(String.class)
        .equals(clientNumber);
  }

  private static Stream<String> saveDba() {
    return Stream.of("00000005", "00000006", "00000007");
  }

}