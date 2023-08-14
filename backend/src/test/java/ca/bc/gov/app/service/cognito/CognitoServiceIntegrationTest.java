package ca.bc.gov.app.service.cognito;

import static ca.bc.gov.app.TestConstants.AUTH_RESPONSE;
import static ca.bc.gov.app.TestConstants.AUTH_RESPONSE_OK;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import reactor.test.StepVerifier;

@DisplayName("Integration Test | Cognito Service")
class CognitoServiceIntegrationTest extends AbstractTestContainerIntegrationTest {

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

  @Autowired
  private CognitoService service;

  @Test
  @DisplayName("should exchange token")
  void shouldExchangeToken(){

    wireMockExtension
        .stubFor(
            post("/oauth2/token")
                .withHeader("Content-Type",equalTo(MediaType.APPLICATION_FORM_URLENCODED_VALUE+";charset=UTF-8"))
                .withHeader("Accept",equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withCookie("XSRF-TOKEN", WireMock.matching("(.*)"))
                .willReturn(okJson(AUTH_RESPONSE_OK))
        );

    service
        .exchangeAuthorizationCodeForTokens(UUID.randomUUID().toString())
        .as(StepVerifier::create)
        .expectNext(AUTH_RESPONSE)
        .verifyComplete();

  }


}