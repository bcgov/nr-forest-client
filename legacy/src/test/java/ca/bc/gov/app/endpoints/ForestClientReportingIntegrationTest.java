package ca.bc.gov.app.endpoints;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@DisplayName("Integrated Test | Forest Client Reporting Controller")
class ForestClientReportingIntegrationTest extends
    AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10001)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();

  @BeforeAll
  public static void setUp() {
    wireMockExtension.resetAll();
  }


  @Test
  @DisplayName("Generate all spreadsheet")
  void shouldGenerateAllSheet() {
    wireMockExtension
        .stubFor(
            get(urlPathEqualTo("/v4/search/topic"))
                .withQueryParam("format",equalTo("json"))
                .withQueryParam("inactive",equalTo("any"))
                .withQueryParam("ordering",equalTo("-score"))
                .withQueryParam("q",matching("^(.*)$"))
            .willReturn(
                aResponse()
                    .withBody(TestConstants.ORGBOOK_TOPIC)
                    .withStatus(200)
                    .withHeader("Content-Type",MediaType.APPLICATION_JSON_VALUE)
                    .withTransformers("response-template")
            )
        );

    client
        .get()
        .uri("/api/reports/all")
        .exchange()
        .expectStatus().isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .expectHeader().valueEquals(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=FC Report All Clients.xlsx");

  }

  @Test
  @DisplayName("Generate business as spreadsheet")
  void shouldGenerateBusinessAsSheet() {
    wireMockExtension
        .stubFor(
            get(urlPathEqualTo("/v4/search/topic"))
                .withQueryParam("format",equalTo("json"))
                .withQueryParam("inactive",equalTo("any"))
                .withQueryParam("ordering",equalTo("-score"))
                .withQueryParam("q",matching("^(.*)$"))
                .willReturn(
                    aResponse()
                        .withBody(TestConstants.ORGBOOK_TOPIC)
                        .withStatus(200)
                        .withHeader("Content-Type",MediaType.APPLICATION_JSON_VALUE)
                        .withTransformers("response-template")
                )
        );

    client
        .get()
        .uri("/api/reports/businessAs")
        .exchange()
        .expectStatus().isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .expectHeader().valueEquals(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=FC Report Business As.xlsx");

  }
}
