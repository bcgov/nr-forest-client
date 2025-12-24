package ca.bc.gov.app.controller.client;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;


@DisplayName("Integrated Test | History Log Controller")
class HistoryLogControllerTest extends AbstractTestContainerIntegrationTest {
  
  @RegisterExtension
  static WireMockExtension legacyStub = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10060)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();
  
  @Test
  @DisplayName("should return history logs from legacy API with correct headers")
  void shouldReturnHistoryLogsFromLegacyApi() {
    String clientNumber = "123456";
    int page = 0;
    int size = 5;

    String legacyResponse = """
        [
          {
            "tableName": "ClientInformation",
            "idx": "1225082",
            "identifierLabel": "Client summary updated",
            "updateTimestamp": "2007-09-14T10:15:41",
            "updateUserid": "JOEL",
            "changeType": "UPD",
            "details": [
              {
                "columnName": "clientName",
                "oldValue": "JOEL PRODUCTS INC",
                "newValue": "JOEL FOREST PRODUCTS INC."
              }
            ],
            "reasons": [
              {
                "actionCode": "NAME",
                "reason": "Correction"
              }
            ]
          }
        ]
      """;

    legacyStub.stubFor(
        get(urlPathMatching("/api/clients/history-logs/" + clientNumber))
            .withQueryParam("page", equalTo(String.valueOf(page)))
            .withQueryParam("size", equalTo(String.valueOf(size)))
            .willReturn(okJson(legacyResponse)
                .withHeader("X-TOTAL-COUNT", "1"))
    );

    client
        .mutateWith(mockJwt())
        .mutateWith(csrf())
        .mutateWith(mockUser().roles(ApplicationConstant.ROLE_EDITOR))
        .get()
        .uri(uriBuilder -> uriBuilder
            .path("/api/clients/history-logs/{clientNumber}")
            .queryParam("page", page)
            .queryParam("size", size)
            .build(clientNumber))
        .exchange()
        .expectStatus().isOk()
        .expectHeader().valueEquals("X-TOTAL-COUNT", "1")
        .expectBody()
        .json(legacyResponse);
  }

}
