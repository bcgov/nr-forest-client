package ca.bc.gov.app.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.jsonResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import ca.bc.gov.app.extensions.AbstractTestContainer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DisplayName("Integrated Test | Processor Controller")
class ProcessControllerIntegrationTest extends AbstractTestContainer {

  @RegisterExtension
  static WireMockExtension legacyStub = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10014)
              .stubRequestLoggingDisabled(false)
              .notifier(new Slf4jNotifier(true))
      )
      .configureStaticDsl(true)
      .build();

  @RegisterExtension
  static WireMockExtension backendStub = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10000)
              .stubRequestLoggingDisabled(false)
              .notifier(new Slf4jNotifier(true))
      )
      .configureStaticDsl(true)
      .build();

  @Test
  @DisplayName("Should submit by staff")
  void shouldSubmitByStaff() {

    legacyStub.resetAll();
    backendStub.resetAll();
    legacyStub
        .stubFor(
            post("/api/clients").willReturn(aResponse().withStatus(201).withBody("01000100"))
        );

    legacyStub
        .stubFor(
            post("/api/locations").willReturn(aResponse().withStatus(201).withBody("01000100"))
        );

    legacyStub
        .stubFor(
            post("/api/contacts").willReturn(aResponse().withStatus(201).withBody("01000100"))
        );

    backendStub
        .stubFor(
            post("/ches/email")
                .withBasicAuth("uat", "thisisasupersecret")
                .willReturn(jsonResponse(
                        "Transaction ID: " + UUID.randomUUID(),
                        200
                    )
                )
        );

    log.info("Should submit by staff");

    client
        .get()
        .uri("/api/processor/368")
        .exchange()
        .expectStatus().isCreated()
        .expectBody()
        .consumeWith(System.out::println)
        .isEmpty();

  }

}