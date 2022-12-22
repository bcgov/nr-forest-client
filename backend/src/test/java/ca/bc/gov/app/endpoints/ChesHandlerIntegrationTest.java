package ca.bc.gov.app.endpoints;

import static com.github.tomakehurst.wiremock.client.WireMock.forbidden;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.client.WireMock.unauthorized;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.dto.ches.ChesRequest;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.service.ches.ChesCommonServicesService;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@Slf4j
@DisplayName("Integrated Test | Ches Controller")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChesHandlerIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;
  @Autowired
  private ChesCommonServicesService service;

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension
      .newInstance()
      .options(wireMockConfig().port(10010))
      .configureStaticDsl(true)
      .build();

  @Test
  @DisplayName("Send email when authorized")
  void shouldSendMailWhenAuth() throws OAuthProblemException, OAuthSystemException {

    mockOAuthSuccess();

    wireMockExtension
        .stubFor(
            post("/chess/uri")
                .willReturn(
                    ok(TestConstants.CHES_SUCCESS_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );


    client
        .post()
        .uri("/api/mail", new HashMap<>())
        .body(Mono.just(new ChesRequest(List.of("jhon@mail.ca"), "simple body")), ChesRequest.class)
        .exchange()

        .expectStatus().isCreated()
        .expectHeader().location("/api/mail/00000000-0000-0000-0000-000000000000")
        .expectBody().isEmpty();

  }

  @Test
  @DisplayName("Do not send emails when not authorized")
  void shoulNotSendMailWhenNotAuth() throws OAuthProblemException, OAuthSystemException {
    mockOAuthFail();

    client
        .post()
        .uri("/api/mail", new HashMap<>())
        .body(Mono.just(new ChesRequest(List.of("jhon@mail.ca"), "simple body")), ChesRequest.class)
        .exchange()

        .expectStatus().isEqualTo(HttpStatusCode.valueOf(412))
        .expectBody().equals("Cannot retrieve a token");
  }

  @Test
  @DisplayName("Do not send emails when token is invalid")
  void shouldNotSendMailWhenTokenInvalid() throws OAuthProblemException, OAuthSystemException {
    mockOAuthSuccess();

    wireMockExtension
        .stubFor(post("/chess/uri").willReturn(unauthorized()));


    client
        .post()
        .uri("/api/mail", new HashMap<>())
        .body(Mono.just(new ChesRequest(List.of("jhon@mail.ca"), "simple body")), ChesRequest.class)
        .exchange()

        .expectStatus().isEqualTo(HttpStatusCode.valueOf(401))
        .expectBody().equals("Provided access token is missing or invalid");
  }

  @Test
  @DisplayName("Do not send emails when you have no role")
  void shouldNotSendMailWhenNoRoleInvalid() throws OAuthProblemException, OAuthSystemException {
    mockOAuthSuccess();

    wireMockExtension
        .stubFor(post("/chess/uri").willReturn(forbidden()));


    client
        .post()
        .uri("/api/mail", new HashMap<>())
        .body(Mono.just(new ChesRequest(List.of("jhon@mail.ca"), "simple body")), ChesRequest.class)
        .exchange()

        .expectStatus().isEqualTo(HttpStatusCode.valueOf(403))
        .expectBody().equals("You don't have the required role to perform this action");
  }


  @Test
  @DisplayName("Send an email with an HTML body")
  void shouldSendMailWithHTMLBody() throws OAuthProblemException, OAuthSystemException {

    mockOAuthSuccess();

    wireMockExtension
        .stubFor(
            post("/chess/uri")
                .willReturn(
                    ok(TestConstants.CHES_SUCCESS_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );


    client
        .post()
        .uri("/api/mail", new HashMap<>())
        .body(Mono.just(new ChesRequest(List.of("jhon@mail.ca"), "<p>I am an HTML</p>")),
            ChesRequest.class)
        .exchange()

        .expectStatus().isCreated()
        .expectHeader().location("/api/mail/00000000-0000-0000-0000-000000000000")
        .expectBody().isEmpty();
  }

  @Test
  @DisplayName("Send an email with text body")
  void shouldSendMailWithTextBody() throws OAuthProblemException, OAuthSystemException {

    mockOAuthSuccess();

    wireMockExtension
        .stubFor(
            post("/chess/uri")
                .willReturn(
                    ok(TestConstants.CHES_SUCCESS_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );


    client
        .post()
        .uri("/api/mail", new HashMap<>())
        .body(Mono.just(new ChesRequest(List.of("jhon@mail.ca"),
            "Thanks for your email\nYou will hear from us soon")), ChesRequest.class)
        .exchange()

        .expectStatus().isCreated()
        .expectHeader().location("/api/mail/00000000-0000-0000-0000-000000000000")
        .expectBody().isEmpty();
  }

  @Test
  @DisplayName("Fail with 422")
  void shouldFailWith422() throws OAuthProblemException, OAuthSystemException {
    mockOAuthSuccess();

    wireMockExtension
        .stubFor(
            post("/chess/uri")
                .willReturn(
                    status(422)
                        .withBody(TestConstants.CHES_422_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );


    client
        .post()
        .uri("/api/mail", new HashMap<>())
        .body(Mono.just(new ChesRequest(List.of("jhon@mail.ca"),
            "Thanks for your email\nYou will hear from us soon")), ChesRequest.class)
        .exchange()

        .expectStatus().isEqualTo(HttpStatusCode.valueOf(422))
        .expectBody().equals("string,Invalid value `encoding`. on utf-8x");
  }

  @Test
  @DisplayName("Fail when no body is provided")
  void shouldSendMailWithNoBody() throws OAuthProblemException, OAuthSystemException {
    mockOAuthSuccess();

    wireMockExtension
        .stubFor(
            post("/chess/uri")
                .willReturn(
                    status(400)
                        .withBody(TestConstants.CHES_400_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );


    client
        .post()
        .uri("/api/mail", new HashMap<>())
        .body(Mono.just(new ChesRequest(List.of("jhon@mail.ca"),
            "Thanks for your email\nYou will hear from us soon")), ChesRequest.class)
        .exchange()

        .expectStatus().isBadRequest()
        .expectBody().equals("string");
  }

  @Test
  @DisplayName("Fail with 500")
  void shouldFailWith500() throws OAuthProblemException, OAuthSystemException {
    mockOAuthSuccess();

    wireMockExtension
        .stubFor(
            post("/chess/uri")
                .willReturn(
                    status(500)
                        .withBody(TestConstants.CHES_500_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );


    client
        .post()
        .uri("/api/mail", new HashMap<>())
        .body(Mono.just(new ChesRequest(List.of("jhon@mail.ca"),
            "Thanks for your email\nYou will hear from us soon")), ChesRequest.class)
        .exchange()

        .expectStatus().isEqualTo(HttpStatusCode.valueOf(500))
        .expectBody().equals("string");
  }

  @DisplayName("Fail when no emailTo is provided")
  void shouldFailWhenNoDestinationMail() {
  }

  @Test
  @DisplayName("Send an email with more than one emailTo provided")
  void shouldSendMailMultipleDestination() throws OAuthProblemException, OAuthSystemException {

    mockOAuthSuccess();

    wireMockExtension
        .stubFor(
            post("/chess/uri")
                .willReturn(
                    ok(TestConstants.CHES_SUCCESS_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );


    client
        .post()
        .uri("/api/mail", new HashMap<>())
        .body(Mono.just(new ChesRequest(List.of("jhon@mail.ca", "james@mail.ca"), "simple body")),
            ChesRequest.class)
        .exchange()

        .expectStatus().isCreated()
        .expectHeader().location("/api/mail/00000000-0000-0000-0000-000000000000")
        .expectBody().isEmpty();
  }


  private void mockOAuthSuccess() throws OAuthSystemException, OAuthProblemException {
    OAuthClient oauth = mock(OAuthClient.class);
    OAuthJSONAccessTokenResponse res = mock(OAuthJSONAccessTokenResponse.class);
    when(oauth.accessToken(any(), any(), any())).thenReturn(res);
    when(res.getAccessToken()).thenReturn("res");
    service.setOauthClient(oauth);
  }

  private void mockOAuthFail() throws OAuthSystemException, OAuthProblemException {
    OAuthClient oauth = mock(OAuthClient.class);

    when(oauth.accessToken(any(), any(), any())).thenThrow(OAuthProblemException.error("Oh oh"));

    service.setOauthClient(oauth);
  }

}
