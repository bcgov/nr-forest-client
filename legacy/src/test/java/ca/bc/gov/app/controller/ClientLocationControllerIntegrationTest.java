package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.ForestClientLocationDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.util.Map;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

@Slf4j
@DisplayName("Integrated Test | Client Location Controller")
class ClientLocationControllerIntegrationTest extends
    AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @ParameterizedTest
  @MethodSource("saveLocation")
  @DisplayName("Save a location")
  void shouldSaveLocation(String clientNumber) {
    client
        .post()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/locations")
                .build(Map.of())
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .body(BodyInserters.fromValue(
                new ForestClientLocationDto(
                    clientNumber,
                    "00",
                    "BILLING ADDRESS",
                    "2975 Jutland Rd.",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "VICTORIA",
                    "BC",
                    "V8V8V8",
                    "CANADA",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "N",
                    null,
                    "Y",
                    StringUtils.EMPTY,
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

  private static Stream<String> saveLocation() {
    return Stream.of("00000001", "00000002", "10000003");
  }

}