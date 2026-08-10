package ca.bc.gov.app.controller;

import static org.assertj.core.api.Assertions.assertThat;
import ca.bc.gov.app.dto.HistoryLogDto;
import ca.bc.gov.app.exception.MissingRequiredParameterException;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

@Slf4j
@DisplayName("Integrated Test | Client History Controller")
public class ClientHistoryControllerIntegrationTest extends 
    AbstractTestContainerIntegrationTest {

  @ParameterizedTest
  @MethodSource("byClientNumber")
  @DisplayName("Get history logs by client number")
  void shouldReturnHistoryLogsByClientNumber(
      String clientNumber,
      String expectedClientNumber,
      boolean expectEmptyWithHeader,
      Class<RuntimeException> exception
  ) {
    
    ResponseSpec response =
        client
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/clients/history-logs/{clientNumber}")
                .queryParam("sources", "cli")
                .build(clientNumber))
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .exchange();

    if (StringUtils.isNotBlank(expectedClientNumber)) {
      response
        .expectStatus().isOk()
        .expectHeader().exists("x-total-count")
        .expectHeader().value("x-total-count",
            count -> assertThat(Integer.parseInt(count)).isGreaterThan(0))
        .expectBodyList(HistoryLogDto.class)
        .value(logs -> assertThat(logs).isNotEmpty());
    }

    if (expectEmptyWithHeader) {
      response
        .expectStatus().isOk()
        .expectHeader().exists("x-total-count")
        .expectHeader().valueEquals("x-total-count", "0")
        .expectBodyList(HistoryLogDto.class)
        .value(logs -> assertThat(logs).isEmpty());
    }

    if (exception != null) {
      response.expectStatus().is4xxClientError();
    }
  }

  private static Stream<Arguments> byClientNumber() {
    return Stream.of(
        // Valid case: client with history logs
        Arguments.of("00000138", "00000138", false, null),

        // Valid case: client with no history logs — x-total-count must still be "0"
        Arguments.of("99999999", null, true, null),

        // Invalid case: missing client number
        Arguments.of(null, null, false, MissingRequiredParameterException.class));
  }
  
}
