package ca.bc.gov.app.controller.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MimeType;

@DisplayName("Integrated Test | Patch Client Controller")
class ClientPatchControllerIntegrationTest
    extends AbstractTestContainerIntegrationTest {

  public static final String PATCH_BODY = "[{\"op\":\"replace\",\"path\":\"/wcbFirmNumber\",\"value\":\"123123\"}]";
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

  @Autowired
  protected WebTestClient client;

  @BeforeEach
  public void setUp() {
    legacyStub.resetAll();
  }

  @Test
  @DisplayName("Should not allow non-editor to patch client")
  void shouldNotAllowNonEditorToPatchClient() {
    client
        .mutateWith(csrf())
        .mutateWith(
            mockJwt()
                .jwt(jwt -> jwt.claims(claims -> claims.putAll(
                    TestConstants.getClaims("idir", ApplicationConstant.ROLE_VIEWER))))
                .authorities(new SimpleGrantedAuthority("ROLE_" + ApplicationConstant.ROLE_VIEWER))
        )
        .patch()
        .uri("/api/clients/details/00123456")
        .contentType(MediaType.asMediaType(new MimeType("application", "json-patch+json")))
        .bodyValue(PATCH_BODY)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  @DisplayName("Should allow editor to patch client")
  void shouldSendPatchRequestToLegacy() {

    legacyStub
        .stubFor(
            get(urlEqualTo("/api/search/clientNumber/00123456"))
                .willReturn(okJson("""
                      {
                        "client":{
                          "clientIdentification":null,
                          "birthdate":null
                        },
                        "addresses":null,
                        "contacts":null,
                        "doingBusinessAs":null
                      }"""))
                .withHeader("Content-Type", equalTo("application/json"))
        );

    legacyStub
        .stubFor(
            patch(urlEqualTo("/api/clients/partial/00123456"))
                .willReturn(aResponse().withStatus(202))
        );

    client
        .mutateWith(csrf())
        .mutateWith(
            mockJwt()
                .jwt(jwt -> jwt.claims(claims -> claims.putAll(
                    TestConstants.getClaims("idir", ApplicationConstant.ROLE_EDITOR))))
                .authorities(new SimpleGrantedAuthority("ROLE_" + ApplicationConstant.ROLE_EDITOR))
        )
        .patch()
        .uri("/api/clients/details/00123456")
        .contentType(MediaType.asMediaType(new MimeType("application", "json-patch+json")))
        .bodyValue(PATCH_BODY)
        .exchange()
        .expectStatus()
        .isAccepted();
  }

  @Test
  @DisplayName("Should fail to patch client when legacy fails")
  void shouldSendNotFoundWhenClientNumberIsNotFound() {

    legacyStub
        .stubFor(
            get(urlEqualTo("/api/search/clientNumber/00123456"))
                .willReturn(aResponse().withStatus(404))
        );

    legacyStub
        .stubFor(
            patch(urlEqualTo("/api/clients/partial/00123456"))
                .willReturn(aResponse().withStatus(404))
        );

    client
        .mutateWith(csrf())
        .mutateWith(
            mockJwt()
                .jwt(jwt -> jwt.claims(claims -> claims.putAll(
                    TestConstants.getClaims("idir", ApplicationConstant.ROLE_EDITOR))))
                .authorities(new SimpleGrantedAuthority("ROLE_" + ApplicationConstant.ROLE_EDITOR))
        )
        .patch()
        .uri("/api/clients/details/00123456")
        .contentType(MediaType.asMediaType(new MimeType("application", "json-patch+json")))
        .bodyValue(PATCH_BODY)
        .exchange()
        .expectStatus()
        .isNotFound();
  }


}