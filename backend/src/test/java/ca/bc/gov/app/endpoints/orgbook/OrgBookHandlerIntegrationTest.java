package ca.bc.gov.app.endpoints.orgbook;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import ca.bc.gov.app.util.CoreUtil;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@DisplayName("Integrated Test | OrgBook Controller")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrgBookHandlerIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10020)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();


  @Test
  @DisplayName("Look for Incorporation with ID 'BC0772006'")
  void shouldGetDataFromIncorporation() {

    wireMockExtension
        .stubFor(
            get(urlPathEqualTo("/v4/search/topic"))
                .withQueryParam("format", equalTo("json"))
                .withQueryParam("inactive", equalTo("any"))
                .withQueryParam("ordering", equalTo("-score"))
                .withQueryParam("q", equalTo("BC0772006"))
                .willReturn(okJson(TestConstants.ORGBOOK_INCORP_OK))
        );

    client
        .get()
        .uri("/api/orgbook/incorporation/BC0772006")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .json(TestConstants.ORGBOOK_INCORP_OK_RESPONSE);

  }

  @Test
  @DisplayName("Look for Incorporation with ID 'BC0000000'")
  void shouldGetNoDataFromIncorporation() {

    wireMockExtension
        .stubFor(
            get(urlPathEqualTo("/v4/search/topic"))
                .withQueryParam("format", equalTo("json"))
                .withQueryParam("inactive", equalTo("any"))
                .withQueryParam("ordering", equalTo("-score"))
                .withQueryParam("q", equalTo("BC0000000"))
                .willReturn(okJson(TestConstants.ORGBOOK_INCORP_EMPTY))
        );

    client
        .get()
        .uri("/api/orgbook/incorporation/BC0000000")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .json(TestConstants.ORGBOOK_INCORP_EMPTY);

  }

  @Test
  @DisplayName("Look for name 'Power Corp'")
  void shouldGetDataFromNameLookup() {

    wireMockExtension
        .stubFor(
            get(urlPathEqualTo("/v3/search/autocomplete"))
                .withQueryParam("q", equalTo(CoreUtil.encodeString("Power")))
                .willReturn(okJson(TestConstants.ORGBOOK_NAMELOOKUP_OK))
        );

    client
        .get()
        .uri("/api/orgbook/name/Power")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .json(TestConstants.ORGBOOK_NAMELOOKUP_OK_RESPONSE);
  }

  @Test
  @DisplayName("Look for name 'Jhon'")
  void shouldGetNoDataFromNameLookup() {

    wireMockExtension
        .stubFor(
            get(urlPathEqualTo("/v3/search/autocomplete"))
                .withQueryParam("q", equalTo(CoreUtil.encodeString("Jhon")))
                .willReturn(okJson(TestConstants.ORGBOOK_INCORP_EMPTY))
        );

    client
        .get()
        .uri("/api/orgbook/name/Jhon")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .json(TestConstants.ORGBOOK_NAMELOOKUP_EMPTY);
  }

}
