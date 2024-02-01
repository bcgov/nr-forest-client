package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.util.Map;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

@Slf4j
@DisplayName("Integrated Test | Client Controller")
class ClientControllerIntegrationTest extends
    AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @ParameterizedTest
  @MethodSource("saveClient")
  @DisplayName("Save a client")
  void shouldSaveLocation(String clientName, String clientNumber) {
    client
        .post()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/clients")
                .build(Map.of())
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .body(BodyInserters.fromValue(
                new ForestClientDto(
                    clientNumber,
                    clientName,
                    null,
                    null,
                    "ACT",
                    "C",
                    null,
                    null,
                    null,
                    "BC",
                    "123456789",
                    "Comment here",
                    "Test",
                    "Test",
                    1L
                )
            )
        )
        .exchange()
        .expectStatus().isCreated()
        .expectBody(String.class)
        .value(StringUtils::isNotBlank);

  }

  private static Stream<Arguments> saveClient() {
    return Stream.of(
        Arguments.of("TESTADOC", null),
        Arguments.of("TESTADOC", "12345678"),
        Arguments.of("SAMPLER FOREST PRODUCTS INC.", null),
        Arguments.of("BORIS AND BORIS INC.", null),
        Arguments.of("CORP. OF THE CITY OF VICTORIA", null)
    );
  }
}
