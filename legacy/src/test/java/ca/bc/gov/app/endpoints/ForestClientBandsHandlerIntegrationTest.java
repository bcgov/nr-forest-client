package ca.bc.gov.app.endpoints;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@DisplayName("Integrated Test | Forest Client Bands Controller")
class ForestClientBandsHandlerIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10000)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();

  @BeforeAll
  public static void setUp(){
    wireMockExtension.resetAll();
  }

  @Test
  @DisplayName("List first nation band validation")
  void shouldListBandValidation() {
    wireMockExtension
        .stubFor(
            get(urlMatching("/api/maps/firstNation/.*"))
                .willReturn(okJson("""
                    {        
                      "FIRST_NATION_FEDERAL_NAME": "Sample Indian Band Council",
                      "FIRST_NATION_FEDERAL_ID": 684,
                      "ADDRESS_LINE1": "916-1150 Mainland St",
                      "ADDRESS_LINE2": " ",
                      "OFFICE_CITY": "VANCOUVER",
                      "OFFICE_PROVINCE": "BC",
                      "OFFICE_POSTAL_CODE": "V6B 2T4"
                    }
                    """)
                )
        );


    client
        .get()
        .uri("/api/client/bands")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("[0].clientNumber").isNotEmpty()
        .jsonPath("[0].clientNumber").isEqualTo("00000004")

        .jsonPath("[0].nameMatch").isNotEmpty()
        .jsonPath("[0].nameMatch").isBoolean()
        .jsonPath("[0].nameMatch").isEqualTo(true)

        .jsonPath("[0].addressMatch").isNotEmpty()
        .jsonPath("[0].addressMatch").isBoolean()
        .jsonPath("[0].addressMatch").isEqualTo(false)

        .consumeWith(System.out::println);


  }

}
