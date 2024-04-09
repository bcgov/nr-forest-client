package ca.bc.gov.app.service.ches;

import static com.github.tomakehurst.wiremock.client.WireMock.forbidden;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.client.WireMock.unauthorized;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.dto.ches.ChesRequestDto;
import ca.bc.gov.app.exception.BadRequestException;
import ca.bc.gov.app.exception.InvalidAccessTokenException;
import ca.bc.gov.app.exception.InvalidRoleException;
import ca.bc.gov.app.exception.UnableToProcessRequestException;
import ca.bc.gov.app.exception.UnexpectedErrorException;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
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

@DisplayName("Integrated Test | Ches Service")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChesServiceIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  private ChesService service;

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension
      .newInstance()
      .options(wireMockConfig().port(10010))
      .configureStaticDsl(true)
      .build();



  @Test
  @DisplayName("Do not send emails when not authorized")
  void shoulNotSendMailWhenNotAuth() {
    mockOAuthFail();

    service
        .sendEmail(new ChesRequestDto(List.of("jhon@mail.ca"), "simple body"), "Test")
        .as(StepVerifier::create)
        .expectError(InvalidAccessTokenException.class)
        .verify();
  }

  @Test
  @DisplayName("Do not send emails when token is invalid")
  void shouldNotSendMailWhenTokenInvalid() {
    mockOAuthSuccess();

    wireMockExtension
        .stubFor(post("/chess/uri/email").willReturn(unauthorized()));

    service
        .sendEmail(new ChesRequestDto(List.of("jhon@mail.ca"), "simple body"), "Test")
        .as(StepVerifier::create)
        .expectError(InvalidAccessTokenException.class)
        .verify();
  }

  @Test
  @DisplayName("Do not send emails when you have no role")
  void shouldNotSendMailWhenNoRoleInvalid() {
    mockOAuthSuccess();

    wireMockExtension
        .stubFor(post("/chess/uri/email").willReturn(forbidden()));

    service
        .sendEmail(new ChesRequestDto(List.of("jhon@mail.ca"), "simple body"), "Test")
        .as(StepVerifier::create)
        .expectError(InvalidRoleException.class)
        .verify();
  }



  @ParameterizedTest
  @MethodSource("mailIsOk")
  @DisplayName("Send OK email")
  void shouldSendMailWhenAuth(ChesRequestDto body) {

    mockOAuthSuccess();

    wireMockExtension
        .stubFor(
            post("/chess/uri/email")
                .willReturn(
                    ok(TestConstants.CHES_SUCCESS_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );

    service
        .sendEmail(body, "Test")
        .as(StepVerifier::create)
        .expectNext("00000000-0000-0000-0000-000000000000")
        .verifyComplete();

  }

  @Test
  @DisplayName("Fail with 422")
  void shouldFailWith422() {
    mockOAuthSuccess();

    wireMockExtension
        .stubFor(
            post("/chess/uri/email")
                .willReturn(
                    status(422)
                        .withBody(TestConstants.CHES_422_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );

    service
        .sendEmail(new ChesRequestDto(List.of("jhon@mail.ca"),
            "Thanks for your email\nYou will hear from us soon"), "Test")
        .as(StepVerifier::create)
        .expectError(UnableToProcessRequestException.class)
        .verify();
  }

  @Test
  @DisplayName("Fail when no body is provided")
  void shouldSendMailWithNoBody() {
    mockOAuthSuccess();

    wireMockExtension
        .stubFor(
            post("/chess/uri/email")
                .willReturn(
                    status(400)
                        .withBody(TestConstants.CHES_400_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );

    service
        .sendEmail(new ChesRequestDto(List.of("jhon@mail.ca"),
            "Thanks for your email\nYou will hear from us soon"), "Test")
        .as(StepVerifier::create)
        .expectError(BadRequestException.class)
        .verify();
  }

  @Test
  @DisplayName("Fail with 500")
  void shouldFailWith500() {
    mockOAuthSuccess();

    wireMockExtension
        .stubFor(
            post("/chess/uri/email")
                .willReturn(
                    status(500)
                        .withBody(TestConstants.CHES_500_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );

    service
        .sendEmail(new ChesRequestDto(List.of("jhon@mail.ca"),
            "Thanks for your email\nYou will hear from us soon"), "Test")
        .as(StepVerifier::create)
        .expectError(UnexpectedErrorException.class)
        .verify();
  }

  @Test
  @DisplayName("Send an email with more than one emailTo provided")
  void shouldSendMailMultipleDestination() {

    mockOAuthSuccess();

    wireMockExtension
        .stubFor(
            post("/chess/uri/email")
                .willReturn(
                    ok(TestConstants.CHES_SUCCESS_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );

    service
        .sendEmail(new ChesRequestDto(List.of("jhon@mail.ca", "james@mail.ca"), "simple body"),
            "Test")
        .as(StepVerifier::create)
        .expectNext("00000000-0000-0000-0000-000000000000")
        .verifyComplete();
  }

  @Test
  @DisplayName("Template was built")
  void shouldBuildTemplate() {
    Map<String, Object> variables = Map.of("business", Map.of(
        "name", "John",
        "registrationNumber", "john@example.com")
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


  private void mockOAuthSuccess() {
    wireMockExtension
        .stubFor(
            post("/token/uri")
                .willReturn(
                    ok(TestConstants.CHES_TOKEN_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );
  }

  private void mockOAuthFail() {
    wireMockExtension
        .stubFor(
            post("/token/uri")
                .willReturn(
                    unauthorized()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );
  }

  private static Stream<ChesRequestDto> mailIsOk(){
    return Stream.of(
        new ChesRequestDto(List.of("jhon@mail.ca"),
            "Thanks for your email\nYou will hear from us soon"),
        new ChesRequestDto(List.of("jhon@mail.ca"), "<p>I am an HTML</p>"),
        new ChesRequestDto(List.of("jhon@mail.ca"), "simple body")
        );

  }

}
