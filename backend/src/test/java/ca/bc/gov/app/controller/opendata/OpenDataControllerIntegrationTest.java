package ca.bc.gov.app.controller.opendata;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;

@DisplayName("Integrated Test | OpenData Controller")
class OpenDataControllerIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @RegisterExtension
  static WireMockExtension mockSac = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(11111)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();

  @RegisterExtension
  static WireMockExtension mockBcMap = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(11112)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();

  @BeforeEach
  public void reset() {
    mockSac.resetAll();
    mockBcMap.resetAll();

    client = client
        .mutateWith(csrf())
        .mutateWith(mockUser().roles(ApplicationConstant.ROLE_EDITOR))
        .mutateWith(
            mockJwt()
                .jwt(jwt -> jwt.claims(claims -> claims.putAll(TestConstants.getClaims("idir"))))
                .authorities(new SimpleGrantedAuthority("ROLE_"+ApplicationConstant.ROLE_EDITOR))
        )
        .mutate()
        .responseTimeout(Duration.ofSeconds(10))
        .build();
  }

  @Test
  @DisplayName("Should search and return BC Maps data")
  void shouldSearchAndReturnBcMapsData() {

    mockBcMap
        .stubFor(
            get(urlPathEqualTo("/bcmaps"))
                .withQueryParam("service", equalTo("WFS"))
                .withQueryParam("request", equalTo("GetFeature"))
                .withQueryParam("typeName", equalTo("WHSE_HUMAN_CULTURAL_ECONOMIC.FN_COMMUNITY_LOCATIONS_SP"))
                .withQueryParam("version", equalTo("1.0.0"))
                .withQueryParam("outputFormat", equalTo("JSON"))
                .withQueryParam("filter", containing("Squamish"))
                .willReturn(okJson(TestConstants.OPENMAPS_BCMAPS_DATA))
        );

    client
        .get()
        .uri("/api/opendata/Squamish")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("$[0].name").isEqualTo("Squamish Nation")
        .jsonPath("$[0].id").isEqualTo(555)
        .jsonPath("$[0].addresses[0].streetAddress").isEqualTo("320 Seymour Boulevard")
        .jsonPath("$[0].addresses[0].locationName").isEqualTo("Mailing Address")
        .consumeWith(System.out::println);


  }

  @Test
  @DisplayName("Should search and return Sac Maps data")
  void shouldSearchAndReturnSacMapsData() {

    mockBcMap
        .stubFor(
            get(urlPathEqualTo("/bcmaps"))
                .withQueryParam("service", equalTo("WFS"))
                .withQueryParam("request", equalTo("GetFeature"))
                .withQueryParam("typeName", equalTo("WHSE_HUMAN_CULTURAL_ECONOMIC.FN_COMMUNITY_LOCATIONS_SP"))
                .withQueryParam("version", equalTo("1.0.0"))
                .withQueryParam("outputFormat", equalTo("JSON"))
                .withQueryParam("filter", containing("Webequie"))
                .willReturn(okJson(TestConstants.OPENMAPS_BCMAPS_NODATA))
        );

    mockSac
        .stubFor(
            get(urlPathEqualTo("/sac"))
                .withQueryParam("service", equalTo("WFS"))
                .withQueryParam("request", equalTo("GetFeature"))
                .withQueryParam("typeName", equalTo("Donnees_Ouvertes-Open_Data_Premiere_Nation_First_Nation:Première_Nation___First_Nation"))
                .withQueryParam("version", equalTo("2.0.0"))
                .withQueryParam("outputFormat", equalTo("GEOJSON"))
                .withQueryParam("filter", containing("Webequie"))
                .willReturn(okJson(TestConstants.OPENMAPS_SAC_DATA))
        );


    client
        .get()
        .uri("/api/opendata/Webequie")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("$[0].name").isEqualTo("Webequie")
        .jsonPath("$[0].id").isEqualTo(240)
        .jsonPath("$[0].addresses.length()").isEqualTo(0)
        .consumeWith(System.out::println);
  }

  @Test
  @DisplayName("Should search and return no data")
  void shouldSearchAndReturnNoFirstNationData() {

    mockBcMap
        .stubFor(
            get(urlPathEqualTo("/bcmaps"))
                .withQueryParam("service", equalTo("WFS"))
                .withQueryParam("request", equalTo("GetFeature"))
                .withQueryParam("typeName", equalTo("WHSE_HUMAN_CULTURAL_ECONOMIC.FN_COMMUNITY_LOCATIONS_SP"))
                .withQueryParam("version", equalTo("1.0.0"))
                .withQueryParam("outputFormat", equalTo("JSON"))
                .withQueryParam("filter", containing("Tupi"))
                .willReturn(okJson(TestConstants.OPENMAPS_BCMAPS_NODATA))
        );

    mockSac
        .stubFor(
            get(urlPathEqualTo("/sac"))
                .withQueryParam("service", equalTo("WFS"))
                .withQueryParam("request", equalTo("GetFeature"))
                .withQueryParam("typeName", equalTo("Donnees_Ouvertes-Open_Data_Premiere_Nation_First_Nation:Première_Nation___First_Nation"))
                .withQueryParam("version", equalTo("2.0.0"))
                .withQueryParam("outputFormat", equalTo("GEOJSON"))
                .withQueryParam("filter", containing("Tupi"))
                .willReturn(okJson(TestConstants.OPENMAPS_SAC_NODATA))
        );


    client
        .get()
        .uri("/api/opendata/Tupi+Guarani")
        .exchange()
        .expectStatus().isOk()
        .expectBody(List.class)
        .isEqualTo(List.of())
        .consumeWith(System.out::println);


  }

}