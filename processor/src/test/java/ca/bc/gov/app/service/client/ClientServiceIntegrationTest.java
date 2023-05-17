package ca.bc.gov.app.service.client;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.integration.support.MessageBuilder;

@DisplayName("Integrated Test | Client Service")
class ClientServiceIntegrationTest extends AbstractTestContainer {

  @SpyBean
  private SubmissionRepository submissionRepository;
  @SpyBean
  private SubmissionMatchDetailRepository submissionMatchDetailRepository;

  @Autowired
  private ClientService service;

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
        );

    await()
        .alias("Submission lookup")
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(() ->
            verify(submissionRepository, times(1)).findById(eq(1))
        );

    await()
        .alias("Submission persistence")
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() ->
            verify(submissionRepository, times(1)).save(any(SubmissionEntity.class))
        );
  }

  @Test
  @DisplayName("reviewed")
  void shouldReviewSubmission() {

    List<MatcherResult> matchers = new ArrayList<>();

    service
        .reviewed(
            MessageBuilder
                .withPayload(matchers)
                .setHeader(ApplicationConstant.SUBMISSION_ID, 1)
                .build()
        );


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