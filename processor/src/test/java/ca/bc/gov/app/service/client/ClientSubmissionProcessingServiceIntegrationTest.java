package ca.bc.gov.app.service.client;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.EmailRequestDto;
import ca.bc.gov.app.entity.SubmissionContactEntity;
import ca.bc.gov.app.entity.SubmissionDetailEntity;
import ca.bc.gov.app.entity.SubmissionEntity;
import ca.bc.gov.app.entity.SubmissionMatchDetailEntity;
import ca.bc.gov.app.entity.SubmissionStatusEnum;
import ca.bc.gov.app.repository.SubmissionContactRepository;
import ca.bc.gov.app.repository.SubmissionDetailRepository;
import ca.bc.gov.app.repository.SubmissionMatchDetailRepository;
import ca.bc.gov.app.repository.SubmissionRepository;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Client Service")
class ClientSubmissionProcessingServiceIntegrationTest {

  private final SubmissionDetailRepository submissionDetailRepository = mock(
      SubmissionDetailRepository.class);
  private final SubmissionRepository submissionRepository = mock(SubmissionRepository.class);
  private final SubmissionMatchDetailRepository submissionMatchDetailRepository = mock(
      SubmissionMatchDetailRepository.class);
  private final SubmissionContactRepository contactRepository = mock(
      SubmissionContactRepository.class);
  private final ClientSubmissionProcessingService service = new ClientSubmissionProcessingService(
      submissionDetailRepository,
      submissionRepository,
      submissionMatchDetailRepository,
      contactRepository
  );

  @ParameterizedTest
  @MethodSource("submissionLoads")
  @DisplayName("submission loads")
  void shouldLoadSubmission(
      Mono<SubmissionEntity> submission,
      Mono<SubmissionDetailEntity> submissionDetail,
      Mono<SubmissionMatchDetailEntity> submissionMatchDetail,
      Message<SubmissionMatchDetailEntity> expected
  ) {

    when(submissionDetailRepository.findBySubmissionId(1)).thenReturn(submissionDetail);
    when(submissionRepository.findById(1)).thenReturn(submission);
    when(submissionMatchDetailRepository.findBySubmissionId(1)).thenReturn(submissionMatchDetail);

    StepVerifier.FirstStep<Message<SubmissionMatchDetailEntity>> test =
        service
            .processSubmission(
                MessageBuilder
                    .withPayload(1)
                    .build()
            )
            .as(StepVerifier::create);

    if (expected != null) {
      test.assertNext(message -> {
            assertEquals(expected.getPayload(), message.getPayload());
            assertEquals(
                expected.getHeaders().get(ApplicationConstant.SUBMISSION_ID),
                message.getHeaders().get(ApplicationConstant.SUBMISSION_ID)
            );
            assertEquals(
                expected.getHeaders().get(ApplicationConstant.SUBMISSION_STATUS),
                message.getHeaders().get(ApplicationConstant.SUBMISSION_STATUS)
            );
            assertEquals(
                expected.getHeaders().get(ApplicationConstant.SUBMISSION_CLIENTID),
                message.getHeaders().get(ApplicationConstant.SUBMISSION_CLIENTID)
            );
            assertEquals(
                expected.getHeaders().get(ApplicationConstant.SUBMISSION_NAME),
                message.getHeaders().get(ApplicationConstant.SUBMISSION_NAME)
            );
          })
          .verifyComplete();
    } else {
      test.verifyComplete();
    }


  }

  @ParameterizedTest
  @MethodSource("notification")
  @DisplayName("notification is sent")
  void shouldNotify(
      SubmissionStatusEnum statusEnum,
      Mono<SubmissionContactEntity> contact,
      Mono<SubmissionDetailEntity> detail,
      Mono<SubmissionMatchDetailEntity> match,
      Object response
  ) {

    when(contactRepository.findFirstBySubmissionId(1)).thenReturn(contact);
    when(submissionDetailRepository.findBySubmissionId(1)).thenReturn(detail);
    when(submissionMatchDetailRepository.findBySubmissionId(1)).thenReturn(match);

    service
        .notificationProcessing(
            MessageBuilder
                .withPayload(SubmissionMatchDetailEntity.builder().build())
                .setHeader(ApplicationConstant.SUBMISSION_STATUS, statusEnum)
                .setHeader(ApplicationConstant.SUBMISSION_ID, 1)
                .build()
        )
        .as(StepVerifier::create)
        .assertNext(message ->
            assertEquals(response, message.getPayload())
        )
        .verifyComplete();
  }

  @Test
  @DisplayName("update match")
  void shouldUpdateMatch() {
    when(submissionMatchDetailRepository.findBySubmissionId(1))
        .thenReturn(Mono.just(SubmissionMatchDetailEntity.builder().build()));
    service
        .updateMatch(MessageBuilder.withPayload(1).build());

    await()
        .alias("Submission matches")
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() -> {
              verify(submissionMatchDetailRepository, atLeastOnce())
                  .save(any(SubmissionMatchDetailEntity.class));
              verify(submissionMatchDetailRepository, atLeastOnce())
                  .findBySubmissionId(eq(1));
            }
        );
  }

  private static Stream<Arguments> submissionLoads() {
    return
        Stream.of(
            Arguments.of(
                Mono.empty(),
                Mono.empty(),
                Mono.empty(),
                null
            ),
            Arguments.of(
                Mono.just(
                    SubmissionEntity.builder().submissionStatus(SubmissionStatusEnum.N).build()),
                Mono.just(SubmissionDetailEntity.builder().organizationName("TEST").build()),
                Mono.empty(),
                MessageBuilder
                    .withPayload(SubmissionMatchDetailEntity.builder().build())
                    .setHeader(ApplicationConstant.SUBMISSION_ID, 1)
                    .setHeader(ApplicationConstant.SUBMISSION_STATUS, SubmissionStatusEnum.N)
                    .setHeader(ApplicationConstant.SUBMISSION_CLIENTID, StringUtils.EMPTY)
                    .setHeader(ApplicationConstant.SUBMISSION_NAME, "TEST")
                    .build()

            ),
            Arguments.of(
                Mono.just(
                    SubmissionEntity.builder().submissionStatus(SubmissionStatusEnum.N).build()),
                Mono.just(SubmissionDetailEntity.builder().organizationName("TEST").build()),
                Mono.just(SubmissionMatchDetailEntity.builder().build()),
                MessageBuilder
                    .withPayload(SubmissionMatchDetailEntity.builder().build())
                    .setHeader(ApplicationConstant.SUBMISSION_ID, 1)
                    .setHeader(ApplicationConstant.SUBMISSION_STATUS, SubmissionStatusEnum.N)
                    .setHeader(ApplicationConstant.SUBMISSION_CLIENTID, StringUtils.EMPTY)
                    .setHeader(ApplicationConstant.SUBMISSION_NAME, "TEST")
                    .build()

            )
        );
  }

  private static Stream<Arguments> notification() {
    return
        Stream
            .of(
                Arguments.of(
                    SubmissionStatusEnum.A,
                    Mono.empty(),
                    Mono.empty(),
                    Mono.empty(),
                    1
                ),
                Arguments.of(
                    SubmissionStatusEnum.R,
                    Mono.just(SubmissionContactEntity.builder().userId("uid1").firstName("Test")
                        .emailAddress("Mail")
                        .build()),
                    Mono.just(SubmissionDetailEntity.builder().organizationName("TEST")
                        .incorporationNumber("XX012345").build()),
                    Mono.just(
                        SubmissionMatchDetailEntity.builder().matchingMessage("Test").build()),
                    new EmailRequestDto(
                        "XX012345",
                        "TEST",
                        "uid1",
                        "Test",
                        "Mail",
                        "rejection",
                        "Failure",
                        Map.of(
                            "userName", "Test",
                            "name", "TEST",
                            "reason", "Test"
                        )
                    )
                )
            );
  }

}