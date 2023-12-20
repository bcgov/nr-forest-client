package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.ForestClientContactDto;
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
@DisplayName("Integrated Test | Client Contact Controller")
class ClientContactControllerIntegrationTest extends
    AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @ParameterizedTest
  @MethodSource("saveContact")
  @DisplayName("Save a contact")
  void shouldSaveLocation(String clientNumber) {
    client
        .post()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/contacts")
                .build(Map.of())
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .body(BodyInserters.fromValue(
                new ForestClientContactDto(
                    clientNumber,
                    "00",
                    "BL",
                    "James",
                    "2502502550",
                    "mail@mail.ca",
                    "Test",
                    "Test",
                    1L
                )
            )
        )
        .exchange()
        .expectStatus().isCreated()
        .expectBody(String.class)
        .equals(clientNumber);
  }

  private static Stream<String> saveContact() {
    return Stream.of("00000001", "00000002", "00000003");
  }

}