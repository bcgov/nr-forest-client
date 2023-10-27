package ca.bc.gov.app.controller.cognito;

import static ca.bc.gov.app.TestConstants.AUTH_RESPONSE_OK;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@DisplayName("Integration Test | Cognito Controller")
class CognitoControllerIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10070)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();

  @ParameterizedTest
  @DisplayName("should login with provider and redirect")
  @MethodSource("loginWithProvider")
  void shouldLoginWithProviderAndRedirect(String provider) {

    client
        .get()
        .uri(builder -> builder
            .path("/login")
            .queryParam("code", provider)
            .build()
        )
        .exchange()
        .expectStatus().isFound()
        .expectHeader().exists("Location");
  }

  @Test
  @DisplayName("should fail if provider is wrong")
  void shouldFailIfProviderIsWrong() {
    client
        .get()
        .uri(builder -> builder
            .path("/login")
            .queryParam("code", "wrong")
            .build()
        )
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
  }


  @Test
  @DisplayName("should logout")
  void shouldLogOut() {
    client
        .get()
        .uri("/logout")
        .exchange()
        .expectStatus().isFound()
        .expectHeader().exists("Location")
        .expectBody().isEmpty();
  }

  @Test
  @DisplayName("refresh token")
  void shouldRefreshToken() {

    wireMockExtension
        .stubFor(
            post("/")
                .withHeader("Content-Type",
                    equalTo("application/x-amz-json-1.1")
                )
                .withHeader("x-amz-target",
                    equalTo("AWSCognitoIdentityProviderService.InitiateAuth")
                )
                .willReturn(okJson(TestConstants.COGNITO_REFRESH))
        );

    client
        .get()
        .uri(builder -> builder
            .path("/refresh")
            .queryParam("code",TestConstants.TOKEN)
            .build()
        )
        .exchange()
        .expectStatus().isFound()
        .expectHeader().exists("Location");
  }

  @Test
  @DisplayName("should get token from callback")
  void shouldGetTokenFromCallback() {

    wireMockExtension
        .stubFor(
            post("/oauth2/token")
                .withHeader("Content-Type",
                    equalTo(MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8"))
                .withHeader("Accept", equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withCookie("XSRF-TOKEN", WireMock.matching("(.*)"))
                .willReturn(okJson(AUTH_RESPONSE_OK))
        );

    client
        .get()
        .uri(builder -> builder
            .path("/callback")
            .queryParam("code", "code")
            .build()
        )
        .exchange()
        .expectStatus().isFound()
        .expectHeader().exists("Location")
        .expectCookie().exists("accessToken")
        .expectCookie().exists("idToken")
        .expectCookie().exists("refreshToken");
  }

  public static Stream<String> loginWithProvider() {
    return Stream.of("idir", "bcsc", "bceidbusiness");
  }

}