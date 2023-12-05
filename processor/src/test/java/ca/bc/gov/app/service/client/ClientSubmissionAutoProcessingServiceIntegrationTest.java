package ca.bc.gov.app.service.client;

import static ca.bc.gov.app.TestConstants.EMAIL_REQUEST;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.entity.client.SubmissionEntity;
import ca.bc.gov.app.entity.client.SubmissionMatchDetailEntity;
import ca.bc.gov.app.extensions.AbstractTestContainer;
import ca.bc.gov.app.repository.client.SubmissionMatchDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import reactor.test.StepVerifier;

@DisplayName("Integrated Test | Client Service")
class ClientSubmissionAutoProcessingServiceIntegrationTest extends AbstractTestContainer {

  @SpyBean
  private SubmissionRepository submissionRepository;
  @SpyBean
  private SubmissionMatchDetailRepository submissionMatchDetailRepository;

  @Autowired
  private ClientSubmissionAutoProcessingService service;

  @Test
  @DisplayName("approved")
  void shouldApproveSubmission() {

    List<MatcherResult> matchers = new ArrayList<>();

    service
        .approved(
            MessageBuilder
                .withPayload(matchers)
                .setHeader(ApplicationConstant.SUBMISSION_ID, 1)
                .build()
        )
        .as(StepVerifier::create)
        .assertNext(message -> Assertions.assertEquals(Integer.valueOf(1), message.getPayload()))
        .verifyComplete();

    await()
        .alias("Submission lookup")
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(() ->
            verify(submissionRepository, atLeastOnce()).findById(eq(1))
        );

    await()
        .alias("Submission persistence")
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() ->
            verify(submissionRepository, atLeastOnce()).save(any(SubmissionEntity.class))
        );
  }

  @Test
  @DisplayName("complete processing")
  void shouldCompleteProcessing() {

    service
        .completeProcessing(
            MessageBuilder
                .withPayload(EMAIL_REQUEST)
                .setHeader(ApplicationConstant.SUBMISSION_ID, 2)
                .build()
        )
        .as(StepVerifier::create)
        .expectNextCount(1)
        .verifyComplete();

    await()
        .alias("Submission matches and loads")
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() ->
            verify(submissionMatchDetailRepository, atLeastOnce()).findBySubmissionId(eq(2))
        );

    await()
        .alias("Submission matches")
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() ->
            verify(submissionMatchDetailRepository, atLeastOnce())
                .save(any(SubmissionMatchDetailEntity.class))
        );

  }

  @Test
  @DisplayName("reviewed")
  void shouldReviewSubmission() {

    List<MatcherResult> matchers = new ArrayList<>();

    Message<List<MatcherResult>> message = MessageBuilder
        .withPayload(matchers)
        .setHeader(ApplicationConstant.SUBMISSION_ID, 1)
        .build();


    service
        .reviewed(message)
        .as(StepVerifier::create)
            .assertNext( received ->
                assertThat(received)
                    .isNotNull()
                    .isInstanceOf(Message.class)
                    .hasFieldOrPropertyWithValue("payload", 1)
                    .hasFieldOrProperty("headers")
                    .extracting(Message::getHeaders, as(InstanceOfAssertFactories.MAP))
                    .isNotNull()
                    .isNotEmpty()
                    .containsKey("id")
                    .containsKey("timestamp")
                    .containsEntry(ApplicationConstant.SUBMISSION_ID, 1)
            )
                .verifyComplete();

    await()
        .alias("Submission lookup")
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(() ->
            verify(submissionRepository, atLeastOnce()).findById(eq(1))
        );

    await()
        .alias("Submission persistence")
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() ->
            verify(submissionRepository, atLeastOnce()).save(any(SubmissionEntity.class))
        );

    await()
        .alias("Submission matches")
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() ->
            verify(submissionMatchDetailRepository, atLeastOnce()).save(
                any(SubmissionMatchDetailEntity.class))
        );
  }


}