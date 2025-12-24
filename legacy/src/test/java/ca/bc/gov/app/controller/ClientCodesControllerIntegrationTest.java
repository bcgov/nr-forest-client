package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.CodeNameDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;

@Slf4j
@DisplayName("Integrated Test | Client Codes Controller")
class ClientCodesControllerIntegrationTest extends
    AbstractTestContainerIntegrationTest {

  @ParameterizedTest
  @MethodSource("getReasonCodes")
  @DisplayName("Retrieve update reasons by client type and action code")
  void shouldGetReasons(String clientTypeCode, String actionCode) {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/codes/update-reasons/{clientTypeCode}/{actionCode}")
                .build(clientTypeCode, actionCode)
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

  @Test
  @DisplayName("Retrieve active client statuses")
  void shouldGetClientStatuses() {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/codes/client-statuses")
                .build()
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBodyList(CodeNameDto.class);
  }

  @Test
  @DisplayName("Retrieve active registry types")
  void shouldGetRegistryTypes() {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/codes/registry-types")
                .build()
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBodyList(CodeNameDto.class);
  }
  
  @Test
  @DisplayName("Retrieve active registry types by client type")
  void shouldGetRegistryTypesByClientType() {
    String clientTypeCode = "C";
    
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/codes/registry-types/{clientTypeCode}")
                .build(clientTypeCode)
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBodyList(CodeNameDto.class);
  }
  
  @Test
  @DisplayName("Retrieve active client types")
  void shouldGetClientTypes() {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/codes/client-types")
                .build()
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBodyList(CodeNameDto.class);
  }
  
  @Test
  @DisplayName("Retrieve active client ID types")
  void shouldGetClientIdTypes() {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/codes/client-id-types")
                .build()
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBodyList(CodeNameDto.class);
  }
  
  @Test
  @DisplayName("Retrieve active relationship types by client type")
  void shouldGetRelationshipTypesByClientType() {
    String clientTypeCode = "C";
    
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/codes/relationship-types/{clientTypeCode}")
                .build(clientTypeCode)
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBodyList(CodeNameDto.class);
  }
  
}