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
import io.r2dbc.postgresql.codec.Json;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import reactor.test.StepVerifier;

@DisplayName("Integrated Test | Client Service")
class ClientSubmissionAutoProcessingServiceIntegrationTest extends AbstractTestContainer {

  @MockitoSpyBean
  private SubmissionRepository submissionRepository;
  @MockitoSpyBean
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
  @DisplayName("approved preserves matching info")
  void shouldPreserveMatchingInfoOnApprove() {

    // Use an existing seeded submission (id=368 has a matching_detail row with {"info":{}})
    final int submissionId = 368;

    SubmissionMatchDetailEntity seed = SubmissionMatchDetailEntity.builder()
        .submissionId(submissionId)
        .createdBy("test")
        .updatedAt(LocalDateTime.now())
        .matchingField(Json.of(
            "{\"info\":{\"name\":\"John Doe\",\"email\":\"john.doe@gov.bc.ca\"},"
                + "\"contact\":\"00190928\"}"
        ))
        .build();

    // Upsert: if a matching detail already exists for the submission, update it; otherwise insert.
    submissionMatchDetailRepository
        .findBySubmissionId(submissionId)
        .flatMap(existing -> {
          SubmissionMatchDetailEntity updated = existing
              .withMatchingField(seed.getMatchingField())
              .withUpdatedAt(LocalDateTime.now())
              .withCreatedBy(existing.getCreatedBy() == null ? "test" : existing.getCreatedBy());
          return submissionMatchDetailRepository.save(updated);
        })
        .switchIfEmpty(submissionMatchDetailRepository.save(seed))
        .as(StepVerifier::create)
        .assertNext(saved -> Assertions.assertEquals(submissionId, saved.getSubmissionId()))
        .verifyComplete();

    service
        .approved(new MessagingWrapper<>(
            new ArrayList<>(),
            Map.of(ApplicationConstant.SUBMISSION_ID, submissionId)
        ))
        .as(StepVerifier::create)
        .assertNext(message ->
            Assertions.assertEquals(Integer.valueOf(submissionId), message.payload())
        )
        .verifyComplete();

    submissionMatchDetailRepository
        .findBySubmissionId(submissionId)
        .as(StepVerifier::create)
        .assertNext(entity -> {
          AssertionsForInterfaceTypes.assertThat(entity.getMatchers())
              .isNotNull()
              .containsKey(ApplicationConstant.MATCHING_INFO)
              .hasSize(1);
        })
        .verifyComplete();
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

    List<MatcherResult> matchers = List.of(
        new MatcherResult("fi", Set.of()),
        new MatcherResult("fa", Set.of()),
        new MatcherResult("fo", Set.of()),
        new MatcherResult("fu", Set.of())

    );

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

  @Test
  @DisplayName("reviewed with matches")
  void shouldReviewSubmissionWithMatches() {

    List<MatcherResult> matchers = List.of(
        new MatcherResult("fi", Set.of("00000000")),
        new MatcherResult("fa", Set.of()),
        new MatcherResult("fo", Set.copyOf(List.of(
            "00000001",
            "00000002",
            "00000003",
            "00000001",
            "00000002",
            "00000003",
            "00000001",
            "00000002",
            "00000003"
        ))),
        new MatcherResult("fu", Set.of())

    );

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
            {
              verify(submissionMatchDetailRepository, atLeastOnce()).save(
                  any(SubmissionMatchDetailEntity.class));

              submissionMatchDetailRepository
                  .findBySubmissionId(1)
                  .as(StepVerifier::create)
                  .assertNext(entity ->
                      AssertionsForInterfaceTypes.assertThat(entity.getMatchers())
                          .isNotNull()
                          .isNotEmpty()
                          .containsKeys("fi", "fo")
                          .doesNotContainKeys("fa", "fu")
                          .containsEntry("fi", "00000000")
                          .matches(m -> m.get("fo").toString().matches("^(?=.*00000001)(?=.*00000002)(?=.*00000003).*$"))
                  )
                  .verifyComplete();
            }
        );
  }


}
