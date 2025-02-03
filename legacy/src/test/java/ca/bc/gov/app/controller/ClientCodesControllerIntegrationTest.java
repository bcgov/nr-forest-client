package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.CodeNameDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
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
  
  @ParameterizedTest
  @MethodSource("getClientStatusCodes")
  @DisplayName("Retrieve active client statuses")
  void shouldGetClientStatuses() {
      client
          .get()
          .uri(uriBuilder ->
              uriBuilder
                  .path("/api/codes/client_statuses")
                  .build()
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .exchange()
          .expectStatus().isOk()
          .expectBodyList(CodeNameDto.class);
  }

  static Stream<Arguments> getClientStatusCodes() {
    return Stream.of(
        Arguments.of("ACT", "Active"), 
        Arguments.of("DAC", "Deactivated"), 
        Arguments.of("DEC", "Deceased"),
        Arguments.of("REC", "Receivership"),
        Arguments.of("SPN", "Suspended")
    );
  }
  
  @ParameterizedTest
  @MethodSource("getRegistryTypeCodes")
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

  static Stream<Arguments> getRegistryTypeCodes() {
    return Stream.of(
        Arguments.of("A", "Extraprovincial Company"),
        Arguments.of("B", "Extraprovincial Company"),
        Arguments.of("BC", "British Columbia Company"), 
        Arguments.of("C", "Continuation In"),
        Arguments.of("CP", "Cooperative Association"),
        Arguments.of("EPR", "Extraprovincial Company"),
        Arguments.of("FOR", "Extraprovincial Company"),
        Arguments.of("LIC", "Extraprovincial Company"),
        Arguments.of("LL", "Limited Liability Partnership"),
        Arguments.of("LP", "Limited Partnership"), 
        Arguments.of("NON", "Non Registered Company"),
        Arguments.of("REG", "Extraprovincial Company"),
        Arguments.of("S", "British Columbia Society"),
        Arguments.of("XCP", "Extraprovincial Cooperative Association"),
        Arguments.of("XL", "Extraprovincial Limited Liability Partnership"),
        Arguments.of("XP", "Extraprovincial Limited Partnership"),
        Arguments.of("XS", "Extraprovincial Society"), 
        Arguments.of("FM", "Sole Proprietorship"),
        Arguments.of("DINA", "Federal First Nations ID")
    );
  }

}