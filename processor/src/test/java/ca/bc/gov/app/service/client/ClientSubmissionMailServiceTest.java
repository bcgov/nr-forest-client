package ca.bc.gov.app.service.client;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.jsonResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import ca.bc.gov.app.TestConstants;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Client Submission Mail Service")
class ClientSubmissionMailServiceTest {

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension
      .newInstance()
      .options(wireMockConfig().port(10010))
      .configureStaticDsl(true)
      .build();

  private final ClientSubmissionMailService service = new ClientSubmissionMailService(
      WebClient
          .builder()
          .baseUrl("http://127.0.0.1:10010")
          .filter(
              basicAuthentication(
                  "uat",
                  "thisisasupersecret"
              )
          )
          .build()
  );

  @BeforeAll
  public static void setUp() {
    wireMockExtension
        .stubFor(
            post("/ches/email")
                .withBasicAuth("uat", "thisisasupersecret")
                .willReturn(jsonResponse(
                        "Transaction ID: " + UUID.randomUUID(),
                        200
                    )
                )
        );
  }

  @Test
  @DisplayName("send an email")
  void shouldSendEmail() {
    service
        .sendMail(TestConstants.EMAIL_REQUEST)
        .as(StepVerifier::create)
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();

    await()
        .alias("Email sent")
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(() ->
            wireMockExtension
                .verify(
                    postRequestedFor(urlEqualTo("/ches/email"))
                        .withHeader("Content-Type", containing(MediaType.APPLICATION_JSON_VALUE))
                        .withBasicAuth(new BasicCredentials("uat", "thisisasupersecret"))
                        .withRequestBody(equalToJson(TestConstants.EMAIL_REQUEST_JSON)
                        )
                )
        );
  }

  @Test
  @DisplayName("preview email on review")
  void shouldPreventReviewMails() {
    service
        .sendMail(TestConstants.EMAIL_REQUEST)
        .as(StepVerifier::create)
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();

    await()
        .alias("Email sent")
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(() -> {
          wireMockExtension
              .verify(
                  1,
                  postRequestedFor(urlEqualTo("/ches/email"))
                      .withHeader("Content-Type", containing(MediaType.APPLICATION_JSON_VALUE))
                      .withBasicAuth(new BasicCredentials("uat", "thisisasupersecret"))
                      .withRequestBody(equalToJson(TestConstants.EMAIL_REQUEST_JSON)
                      )
              );
        });
  }

}
