package ca.bc.gov.app.service;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.extensions.AbstractTestContainer;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageHeaders;

@DisplayName("Integration Test | Processor End-to-End Flow")
class ProcessorIntegrationTest extends AbstractTestContainer {

  @SpyBean
  private SubmissionRepository submissionRepository;

  @SpyBean
  private SubmissionDetailRepository detailRepository;

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension
      .newInstance()
      .options(wireMockConfig().port(10014))
      .configureStaticDsl(true)
      .build();

  @Autowired
  @Qualifier(ApplicationConstant.SUBMISSION_LIST_CHANNEL)
  private FluxMessageChannel submissionListChannel;

  @Test
  @DisplayName("Process one entity")
  void test() {
    submissionListChannel.send(
        MessageBuilder
            .withPayload(1)
            .setReplyChannelName(ApplicationConstant.SUBMISSION_LIST_CHANNEL)
            .setHeader("output-channel", ApplicationConstant.SUBMISSION_LIST_CHANNEL)
            .setHeader(MessageHeaders.REPLY_CHANNEL, ApplicationConstant.SUBMISSION_LIST_CHANNEL)
            .build()
    );

    wireMockExtension.resetAll();
    wireMockExtension
        .stubFor(
            get(urlPathEqualTo("/api/search/match"))
                .willReturn(okJson("[]"))
        );

    wireMockExtension
        .stubFor(
            get(urlPathEqualTo("/api/search/incorporationOrName"))
                .willReturn(okJson("[]"))
        );

    await()
        .alias("Load details")
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() -> verify(detailRepository, atLeastOnce()).findBySubmissionId(eq(1)));

    await()
        .alias("Match by incorporation number")
            .atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> wireMockExtension.verify(getRequestedFor(urlPathEqualTo("/api/search/match"))));

    await()
        .alias("Match by incorporation number")
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() -> wireMockExtension.verify(getRequestedFor(urlPathEqualTo("/api/search/incorporationOrName"))));

  }
}
