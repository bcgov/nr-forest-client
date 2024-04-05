package ca.bc.gov.app.controller.ches;

import static ca.bc.gov.app.TestConstants.EMAIL_REQUEST;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

import ca.bc.gov.app.ApplicationConstant;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

  @BeforeEach
  public void init() {

    chesStub
        .stubFor(
            post("/chess/uri/mail")
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
        .mutateWith(csrf())
        .mutateWith(mockUser().roles(ApplicationConstant.USERTYPE_SERVICE_USER))
        .post()
        .uri("/api/ches/email")
        .body(Mono.just(EMAIL_REQUEST), EmailRequestDto.class)
        .exchange()
        .expectStatus().isAccepted()
        .expectBody(String.class)
        .isEqualTo("Email sent successfully. Transaction ID: 00000000-0000-0000-0000-000000000000");
  }

  @Test
  @DisplayName("Does not accept unauthorized request")
  void shouldRejectUnauthenticated() {
    client
        .mutateWith(csrf())
        .post()
        .uri("/api/ches/email")
        .body(Mono.just(EMAIL_REQUEST), EmailRequestDto.class)
        .exchange()
        .expectStatus().isUnauthorized();
  }

  @Test
  @DisplayName("Does not accept forbidden request")
  void shouldRejectForbidden() {
    client
        .mutateWith(csrf())
        .mutateWith(mockUser().roles(ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER))
        .post()
        .uri("/api/ches/email")
        .body(Mono.just(EMAIL_REQUEST), EmailRequestDto.class)
        .exchange()
        .expectStatus().isForbidden();
  }

  @Test
  @DisplayName("Send an email for already existing client")
  void shouldSendEmail() {
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

    ///search/registrationOrName?registrationNumber=XX1234567&companyName=Example%20Inc.
    legacyStub
        .stubFor(
            get(urlPathEqualTo("/search/registrationOrName"))
                .withQueryParam("registrationNumber",
                    equalTo(TestConstants.EMAIL_REQUEST.registrationNumber()))
                .withQueryParam("companyName", equalTo(TestConstants.EMAIL_REQUEST.name()))
                .willReturn(okJson(TestConstants.LEGACY_OK))
        );

    client
        .mutateWith(csrf())
        .mutateWith(
            mockJwt()
                .jwt(jwt -> jwt.claims(claims -> claims.putAll(TestConstants.getClaims("bceidbusiness"))))
                .authorities(new SimpleGrantedAuthority("ROLE_" + ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER))
        )
        .post()
        .uri("/api/ches/duplicate")
        .body(Mono.just(TestConstants.EMAIL_REQUEST), EmailRequestDto.class)
        .exchange()
        .expectStatus().isAccepted()
        .expectBody().isEmpty();

  }

}