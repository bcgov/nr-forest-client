package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.CodeNameDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.util.Map;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@DisplayName("Integrated Test | Client Location Controller")
class ClientCodesControllerIntegrationTest extends
    AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @ParameterizedTest
  @MethodSource("getReasonCodes")
  @DisplayName("Get reason codes")
  void shouldGetReasons(
      String clientTypeCode,
      String actionCode
  ) {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/codes/update-reasons")
                .queryParam("clientTypeCode", clientTypeCode)
                .queryParam("actionCode", actionCode)
                .build(Map.of())
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBodyList(CodeNameDto.class);
  }
  
  static Stream<Arguments> getReasonCodes() {
    return Stream.of(
        Arguments.of("C", "NAME"),
        Arguments.of("I", "NAME"),
        Arguments.of("I", "ID")
    );
  }

}