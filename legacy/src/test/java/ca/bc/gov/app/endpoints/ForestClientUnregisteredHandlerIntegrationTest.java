package ca.bc.gov.app.endpoints;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
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
@DisplayName("Integrated Test | Forest Client Unregistered Controller")
class ForestClientUnregisteredHandlerIntegrationTest extends
    AbstractTestContainerIntegrationTest {


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
  public static void setUp() {
    wireMockExtension.resetAll();
  }


  @Test
  @DisplayName("I'm not registered")
  void shouldListBandValidation() {
    wireMockExtension
        .stubFor(get(urlMatching("/api/orgbook/name/.*")).willReturn(notFound()));

    client
        .get()
        .uri("/api/client/unregistered")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBody()

        .jsonPath("$.length()").isNumber()
        .jsonPath("$.length()").isEqualTo(4)

        .consumeWith(System.out::println);


  }
}
