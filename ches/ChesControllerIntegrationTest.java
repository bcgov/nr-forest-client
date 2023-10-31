package ca.bc.gov.app.controller.ches;

import static ca.bc.gov.app.TestConstants.EMAIL_REQUEST;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.dto.client.EmailRequestDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@DisplayName("Integrated Test | FSA Client Email Controller")
class ChesControllerIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @RegisterExtension
  static WireMockExtension chesStub = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10010)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();

  @BeforeEach
  public void init() {

    chesStub
        .stubFor(
            post("/chess/uri")
                .willReturn(
                    ok(TestConstants.CHES_SUCCESS_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );

    chesStub
        .stubFor(
            post("/token/uri")
                .willReturn(
                    ok(TestConstants.CHES_TOKEN_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );

    client = client.mutate()
        .responseTimeout(Duration.ofSeconds(10))
        .build();
  }

  @Test
  @DisplayName("Submit email request")
  void shouldSubmitRegisteredBusinessData() {
    client
        .post()
        .uri("/api/ches/email")
        .body(Mono.just(EMAIL_REQUEST), EmailRequestDto.class)
        .exchange()
        .expectStatus().isAccepted()
        .expectBody().isEmpty();
  }

}