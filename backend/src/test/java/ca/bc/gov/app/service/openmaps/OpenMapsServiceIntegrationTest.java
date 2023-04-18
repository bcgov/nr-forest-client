package ca.bc.gov.app.service.openmaps;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.exception.NoFirstNationException;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

@Slf4j
@DisplayName("Integrated Test | OpenMaps Service")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OpenMapsServiceIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  private OpenMapsService service;

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10030)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();


  @Test
  @DisplayName("Look for first nation with ID '656'")
  void shouldGetDataFromFirstNation() {

    wireMockExtension
        .stubFor(
            get(urlPathEqualTo("/geo/pub/ows"))
                .withQueryParam("service", equalTo("WFS"))
                .withQueryParam("version", equalTo("2.0.0"))
                .withQueryParam("request", equalTo("GetFeature"))
                .withQueryParam("typeName",
                    equalTo("WHSE_HUMAN_CULTURAL_ECONOMIC.FN_COMMUNITY_LOCATIONS_SP"))
                .withQueryParam("count", equalTo("10000"))
                .withQueryParam("CQL_FILTER",
                    equalTo("FIRST_NATION_FEDERAL_ID=656"))
                .withQueryParam("outputFormat", equalTo("json"))
                .willReturn(okJson(TestConstants.OPENMAPS_OK))
        );

    service
        .getFirstNation("656")
        .as(StepVerifier::create)
        .expectNext(TestConstants.OPENMAPS_OK_RESPONSE)
        .verifyComplete();

  }

  @Test
  @DisplayName("Look for first nation with ID '000'")
  void shouldGetNoDataFromIncorporation() {

    wireMockExtension
        .stubFor(
            get(urlPathEqualTo("/geo/pub/ows"))
                .withQueryParam("service", equalTo("WFS"))
                .withQueryParam("version", equalTo("2.0.0"))
                .withQueryParam("request", equalTo("GetFeature"))
                .withQueryParam("typeName",
                    equalTo("WHSE_HUMAN_CULTURAL_ECONOMIC.FN_COMMUNITY_LOCATIONS_SP"))
                .withQueryParam("count", equalTo("10000"))
                .withQueryParam("CQL_FILTER",
                    equalTo("FIRST_NATION_FEDERAL_ID=000"))
                .withQueryParam("outputFormat", equalTo("json"))
                .willReturn(okJson(TestConstants.OPENMAPS_EMPTY))
        );
    service
        .getFirstNation("000")
        .as(StepVerifier::create)
        .expectError(NoFirstNationException.class)
        .verify();

  }

}
