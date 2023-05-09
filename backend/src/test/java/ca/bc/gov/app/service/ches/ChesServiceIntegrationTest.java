package ca.bc.gov.app.service.ches;

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
import ca.bc.gov.app.exception.BadRequestException;
import ca.bc.gov.app.exception.CannotExtractTokenException;
import ca.bc.gov.app.exception.InvalidAccessTokenException;
import ca.bc.gov.app.exception.InvalidRoleException;
import ca.bc.gov.app.exception.UnableToProcessRequestException;
import ca.bc.gov.app.exception.UnexpectedErrorException;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import reactor.test.StepVerifier;

@Slf4j
@DisplayName("Integrated Test | Ches Service")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChesServiceIntegrationTest extends AbstractTestContainerIntegrationTest {

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

    service
        .sendEmail(new ChesRequest(List.of("jhon@mail.ca"), "simple body"))
        .as(StepVerifier::create)
        .expectNext("00000000-0000-0000-0000-000000000000")
        .verifyComplete();

  }

  @Test
  @DisplayName("Do not send emails when not authorized")
  void shoulNotSendMailWhenNotAuth() throws OAuthProblemException, OAuthSystemException {
    mockOAuthFail();

    service
        .sendEmail(new ChesRequest(List.of("jhon@mail.ca"), "simple body"))
        .as(StepVerifier::create)
        .expectError(CannotExtractTokenException.class)
        .verify();
  }

  @Test
  @DisplayName("Do not send emails when token is invalid")
  void shouldNotSendMailWhenTokenInvalid() throws OAuthProblemException, OAuthSystemException {
    mockOAuthSuccess();

    wireMockExtension
        .stubFor(post("/chess/uri").willReturn(unauthorized()));


    service
        .sendEmail(new ChesRequest(List.of("jhon@mail.ca"), "simple body"))
        .as(StepVerifier::create)
        .expectError(InvalidAccessTokenException.class)
        .verify();
  }

  @Test
  @DisplayName("Do not send emails when you have no role")
  void shouldNotSendMailWhenNoRoleInvalid() throws OAuthProblemException, OAuthSystemException {
    mockOAuthSuccess();

    wireMockExtension
        .stubFor(post("/chess/uri").willReturn(forbidden()));

    service
        .sendEmail(new ChesRequest(List.of("jhon@mail.ca"), "simple body"))
        .as(StepVerifier::create)
        .expectError(InvalidRoleException.class)
        .verify();
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

    service
        .sendEmail(new ChesRequest(List.of("jhon@mail.ca"), "<p>I am an HTML</p>"))
        .as(StepVerifier::create)
        .expectNext("00000000-0000-0000-0000-000000000000")
        .verifyComplete();
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

    service
        .sendEmail(
            new ChesRequest(List.of("jhon@mail.ca"),
                "Thanks for your email\nYou will hear from us soon"))
        .as(StepVerifier::create)
        .expectNext("00000000-0000-0000-0000-000000000000")
        .verifyComplete();
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

    service
        .sendEmail(new ChesRequest(List.of("jhon@mail.ca"),
            "Thanks for your email\nYou will hear from us soon"))
        .as(StepVerifier::create)
        .expectError(UnableToProcessRequestException.class)
        .verify();
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

    service
        .sendEmail(new ChesRequest(List.of("jhon@mail.ca"),
            "Thanks for your email\nYou will hear from us soon"))
        .as(StepVerifier::create)
        .expectError(BadRequestException.class)
        .verify();
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

    service
        .sendEmail(new ChesRequest(List.of("jhon@mail.ca"),
            "Thanks for your email\nYou will hear from us soon"))
        .as(StepVerifier::create)
        .expectError(UnexpectedErrorException.class)
        .verify();
  }

  @ParameterizedTest
  @MethodSource("invalidBodies")
  @DisplayName("Fail when body is invalid")
  void shouldFailWhenInvalidBodyProvided(ChesRequest request)
      throws OAuthProblemException, OAuthSystemException {

    mockOAuthSuccess();

    wireMockExtension
        .stubFor(
            post("/chess/uri")
                .willReturn(
                    ok(TestConstants.CHES_SUCCESS_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );

    service
        .sendEmail(request)
        .as(StepVerifier::create)
        .expectError()
        .verify();

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

    service
        .sendEmail(new ChesRequest(List.of("jhon@mail.ca", "james@mail.ca"), "simple body"))
        .as(StepVerifier::create)
        .expectNext("00000000-0000-0000-0000-000000000000")
        .verifyComplete();
  }

  @Test
  @DisplayName("Template was built")
  void shouldBuildTemplate() {
    Map<String, Object> variables = Map.of("business",Map.of(
        "name", "John",
        "incorporation", "john@example.com")
    );
    String expected = """
        <html>
        <body><p>Hi John,</p>
        <p>Thanks for registering with us. We'll be in touch at john@example.com.</p></body>
        </html>""";

    service
        .buildTemplate("test", variables)
        .as(StepVerifier::create)
        .expectNext(expected)
        .verifyComplete();
  }

  @Test
  @DisplayName("Fail when invalid template")
  void testBuildTemplate_InvalidTemplateName() {

    Map<String, Object> variables = Map.of(
        "name", "John",
        "email", "john@example.com");

    service
        .buildTemplate("invalid-template-name", variables)
        .as(StepVerifier::create)
        .expectErrorMatches(t -> t.getMessage().contains("invalid-template-name.html"))
        .verify();
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


  private static Stream<ChesRequest> invalidBodies() {
    return
        Stream
            .of(
                null,
                new ChesRequest(null, null),
                new ChesRequest(null, "goat"),
                new ChesRequest(List.of(), null),
                new ChesRequest(List.of(), "goat"),
                new ChesRequest(List.of("jhon@mail.ca"), null)
            );
  }
}
