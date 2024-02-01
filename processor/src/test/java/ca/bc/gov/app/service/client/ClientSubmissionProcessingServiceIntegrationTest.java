package ca.bc.gov.app.service.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.entity.SubmissionDetailEntity;
import ca.bc.gov.app.entity.SubmissionEntity;
import ca.bc.gov.app.entity.SubmissionMatchDetailEntity;
import ca.bc.gov.app.entity.SubmissionStatusEnum;
import ca.bc.gov.app.repository.SubmissionDetailRepository;
import ca.bc.gov.app.repository.SubmissionMatchDetailRepository;
import ca.bc.gov.app.repository.SubmissionRepository;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Client Service")
class ClientSubmissionProcessingServiceIntegrationTest {

  private final SubmissionDetailRepository submissionDetailRepository = mock(
      SubmissionDetailRepository.class);
  private final SubmissionRepository submissionRepository = mock(SubmissionRepository.class);
  private final SubmissionMatchDetailRepository submissionMatchDetailRepository = mock(
      SubmissionMatchDetailRepository.class);
  private final ClientSubmissionProcessingService service = new ClientSubmissionProcessingService(
      submissionDetailRepository,
      submissionRepository,
      submissionMatchDetailRepository
  );

  @ParameterizedTest
  @MethodSource("submissionLoads")
  @DisplayName("submission loads")
  void shouldLoadSubmission(
      Mono<SubmissionEntity> submission,
      Mono<SubmissionDetailEntity> submissionDetail,
      Mono<SubmissionMatchDetailEntity> submissionMatchDetail,
      MessagingWrapper<SubmissionMatchDetailEntity> expected
  ) {

    when(submissionDetailRepository.findBySubmissionId(1)).thenReturn(submissionDetail);
    when(submissionRepository.findById(1)).thenReturn(submission);
    when(submissionMatchDetailRepository.findBySubmissionId(1)).thenReturn(submissionMatchDetail);

    StepVerifier.FirstStep<MessagingWrapper<SubmissionMatchDetailEntity>> test =
        service
            .processSubmission(new MessagingWrapper<>(1, Map.of()))
            .as(StepVerifier::create);

    if (expected != null) {
      test.assertNext(message -> {
            assertEquals(expected.payload(), message.payload());
            assertEquals(
                expected.parameters().get(ApplicationConstant.SUBMISSION_ID),
                message.parameters().get(ApplicationConstant.SUBMISSION_ID)
            );
            assertEquals(
                expected.parameters().get(ApplicationConstant.SUBMISSION_STATUS),
                message.parameters().get(ApplicationConstant.SUBMISSION_STATUS)
            );
            assertEquals(
                expected.parameters().get(ApplicationConstant.SUBMISSION_CLIENTID),
                message.parameters().get(ApplicationConstant.SUBMISSION_CLIENTID)
            );
            assertEquals(
                expected.parameters().get(ApplicationConstant.SUBMISSION_NAME),
                message.parameters().get(ApplicationConstant.SUBMISSION_NAME)
            );
          })
          .verifyComplete();
    } else {
      test.verifyComplete();
    }


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
                new MessagingWrapper<>(
                    SubmissionMatchDetailEntity.builder().build(),
                    Map.of(
                        ApplicationConstant.SUBMISSION_ID, 1,
                        ApplicationConstant.SUBMISSION_STATUS, SubmissionStatusEnum.N,
                        ApplicationConstant.SUBMISSION_CLIENTID, StringUtils.EMPTY,
                        ApplicationConstant.SUBMISSION_NAME, "TEST"
                    )
                )

            ),
            Arguments.of(
                Mono.just(
                    SubmissionEntity.builder().submissionStatus(SubmissionStatusEnum.N).build()),
                Mono.just(SubmissionDetailEntity.builder().organizationName("TEST").build()),
                Mono.just(SubmissionMatchDetailEntity.builder().build()),
                new MessagingWrapper<>(
                    SubmissionMatchDetailEntity.builder().build(),
                    Map.of(
                        ApplicationConstant.SUBMISSION_ID, 1,
                        ApplicationConstant.SUBMISSION_STATUS, SubmissionStatusEnum.N,
                        ApplicationConstant.SUBMISSION_CLIENTID, StringUtils.EMPTY,
                        ApplicationConstant.SUBMISSION_NAME, "TEST"
                    )

                )
            )
        );
  }

}
