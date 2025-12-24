package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.entity.ClientRelatedProjection;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.util.Map;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

@Slf4j
@DisplayName("Integrated Test | Client Controller")
@TestInstance(Lifecycle.PER_CLASS)
class ClientControllerIntegrationTest extends
    AbstractTestContainerIntegrationTest {

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
                    1L,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY
                )
            )
        )
        .exchange()
        .expectStatus().isCreated()
        .expectBody(String.class)
        .value(StringUtils::isNotBlank);

  }

  @ParameterizedTest
  @MethodSource("related")
  @DisplayName("List related clients")
  void shouldListRelatedClients(String clientNumber, int size) {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder.path("/api/clients/{clientNumber}/related-clients")
                .build(Map.of("clientNumber", clientNumber))
            )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBodyList(ClientRelatedProjection.class)
        .hasSize(size)
        .consumeWith(System.out::println);
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

  private static Stream<Arguments> related() {
    return Stream.of(
        Arguments.of("00000002", 2),
        Arguments.of("00000001", 1),
        Arguments.of("00000145", 0)
    );
  }
}
