package ca.bc.gov.app.service.client;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.entity.SubmissionEntity;
import ca.bc.gov.app.entity.SubmissionMatchDetailEntity;
import ca.bc.gov.app.extensions.AbstractTestContainer;
import ca.bc.gov.app.repository.SubmissionMatchDetailRepository;
import ca.bc.gov.app.repository.SubmissionRepository;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
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
            new MessagingWrapper<>(
                matchers,
                Map.of(ApplicationConstant.SUBMISSION_ID, 1)
            )
        )
        .as(StepVerifier::create)
        .assertNext(message -> Assertions.assertEquals(Integer.valueOf(1), message.payload()))
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
        .completeProcessing(2)
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

    MessagingWrapper<List<MatcherResult>> message = new MessagingWrapper<>(
        matchers, Map.of(ApplicationConstant.SUBMISSION_ID, 1)
    );

    service
        .reviewed(message)
        .as(StepVerifier::create)
        .assertNext(received ->
            assertThat(received)
                .isNotNull()
                .isInstanceOf(MessagingWrapper.class)
                .hasFieldOrPropertyWithValue("payload", 1)
                .hasFieldOrProperty("parameters")
                .extracting(MessagingWrapper::parameters, as(InstanceOfAssertFactories.MAP))
                .isNotNull()
                .isNotEmpty()
                .containsKey("submission-id")
                .containsKey("submission-status")
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
