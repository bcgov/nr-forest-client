package ca.bc.gov.app.service.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.configuration.ForestClientConfiguration;
import ca.bc.gov.app.dto.EmailRequestDto;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.dto.SubmissionProcessKindEnum;
import ca.bc.gov.app.dto.SubmissionProcessTypeEnum;
import ca.bc.gov.app.entity.SubmissionMatchDetailEntity;
import ca.bc.gov.app.entity.SubmissionStatusEnum;
import ca.bc.gov.app.repository.SubmissionRepository;
import ca.bc.gov.app.service.client.ClientSubmissionAutoProcessingService;
import ca.bc.gov.app.service.client.ClientSubmissionLoadingService;
import ca.bc.gov.app.service.client.ClientSubmissionMailService;
import ca.bc.gov.app.service.client.ClientSubmissionProcessingService;
import ca.bc.gov.app.service.legacy.LegacyLoadingService;
import ca.bc.gov.app.service.legacy.LegacyPersistenceService;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Processor Service")
class ProcessorServiceTest {

  private SubmissionRepository submissionRepository;
  private ClientSubmissionLoadingService submissionLoadingService;
  private LegacyLoadingService legacyLoadingService;
  private ClientSubmissionAutoProcessingService autoProcessingService;
  private ClientSubmissionProcessingService submissionProcessingService;
  private ClientSubmissionMailService mailService;
  private LegacyPersistenceService legacyPersistenceService;
  private ForestClientConfiguration configuration;

  private ProcessorService service;

  @BeforeEach
  void setUp() {
    submissionRepository = mock(SubmissionRepository.class);
    submissionLoadingService = mock(ClientSubmissionLoadingService.class);
    legacyLoadingService = mock(LegacyLoadingService.class);
    autoProcessingService = mock(ClientSubmissionAutoProcessingService.class);
    submissionProcessingService = mock(ClientSubmissionProcessingService.class);
    mailService = mock(ClientSubmissionMailService.class);
    legacyPersistenceService = mock(LegacyPersistenceService.class);
    configuration = ForestClientConfiguration.builder()
        .processor(
            ForestClientConfiguration.ProcessorConfiguration.builder()
                .executionTimeout(Duration.ofSeconds(10))
                .build()
        )
        .build();

    service = new ProcessorService(
        submissionRepository,
        submissionLoadingService,
        legacyLoadingService,
        autoProcessingService,
        submissionProcessingService,
        mailService,
        legacyPersistenceService,
        configuration
    );
    StepVerifier.setDefaultTimeout(Duration.ofSeconds(5));
  }

  @Test
  @DisplayName("Process submission messages when auto-approved")
  void shouldProcessSubmissionMessagesAutoApproved() {
    when(submissionRepository.loadProcessingSubmissions())
        .thenReturn(Flux.just(1));

    when(submissionLoadingService.loadSubmissionDetails(1))
        .thenReturn(Mono.just(
            new MessagingWrapper<>(
                TestConstants.SUBMISSION_INFORMATION,
                Map.of(ApplicationConstant.SUBMISSION_ID, 1)
            )
        ));

    when(legacyLoadingService.matchCheck(any()))
        .thenReturn(Mono.just(
            new MessagingWrapper<>(
                List.of(),
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 1,
                    ApplicationConstant.SUBMISSION_STATUS, true
                )
            )
        ));

    when(autoProcessingService.approved(any()))
        .thenReturn(Mono.just(
            new MessagingWrapper<>(
                1,
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 1,
                    ApplicationConstant.SUBMISSION_TYPE, SubmissionProcessTypeEnum.EXTERNAL
                )
            )
        ));

    when(autoProcessingService.reviewed(any()))
        .thenReturn(Mono.empty());

    when(submissionLoadingService.buildMailMessage(any()))
        .thenReturn(Mono.just(TestConstants.EMAIL_REQUEST));

    when(mailService.sendMail(any(EmailRequestDto.class)))
        .thenReturn(Mono.just("OK"));

    service.submissionMessages();

    verify(submissionRepository, times(1)).loadProcessingSubmissions();
    verify(submissionLoadingService, times(1)).loadSubmissionDetails(1);
    verify(legacyLoadingService, times(1)).matchCheck(any());
    verify(autoProcessingService, times(1)).approved(any());
    verify(submissionLoadingService, times(1)).buildMailMessage(any());
    verify(mailService, times(1)).sendMail(TestConstants.EMAIL_REQUEST);
  }

  @Test
  @DisplayName("Process submission messages when sent to review")
  void shouldProcessSubmissionMessagesReviewed() {
    when(submissionRepository.loadProcessingSubmissions())
        .thenReturn(Flux.just(2));

    when(submissionLoadingService.loadSubmissionDetails(2))
        .thenReturn(Mono.just(
            new MessagingWrapper<>(
                TestConstants.SUBMISSION_INFORMATION,
                Map.of(ApplicationConstant.SUBMISSION_ID, 2)
            )
        ));

    when(legacyLoadingService.matchCheck(any()))
        .thenReturn(Mono.just(
            new MessagingWrapper<>(
                List.of(),
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 2,
                    ApplicationConstant.SUBMISSION_STATUS, false
                )
            )
        ));

    when(autoProcessingService.approved(any()))
        .thenReturn(Mono.empty());

    when(autoProcessingService.reviewed(any()))
        .thenReturn(Mono.just(
            new MessagingWrapper<>(
                2,
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 2,
                    ApplicationConstant.SUBMISSION_TYPE, SubmissionProcessTypeEnum.EXTERNAL
                )
            )
        ));

    when(submissionLoadingService.buildMailMessage(any()))
        .thenReturn(Mono.just(TestConstants.EMAIL_REQUEST));

    when(mailService.sendMail(any(EmailRequestDto.class)))
        .thenReturn(Mono.just("OK"));

    service.submissionMessages();

    verify(submissionRepository, times(1)).loadProcessingSubmissions();
    verify(submissionLoadingService, times(1)).loadSubmissionDetails(2);
    verify(legacyLoadingService, times(1)).matchCheck(any());
    verify(autoProcessingService, times(1)).reviewed(any());
    verify(autoProcessingService, never()).approved(any());
    verify(submissionLoadingService, times(1)).buildMailMessage(any());
    verify(mailService, times(1)).sendMail(TestConstants.EMAIL_REQUEST);
  }

  @Test
  @DisplayName("Process processed messages scheduled job")
  void shouldProcessProcessedMessagesScheduled() {
    when(submissionRepository.loadProcessedSubmissions())
        .thenReturn(Flux.just(3));

    when(autoProcessingService.loadMatchingInfo(any()))
        .thenReturn(Mono.just(
            new MessagingWrapper<>(
                3,
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 3,
                    ApplicationConstant.SUBMISSION_STARTER, SubmissionProcessTypeEnum.EXTERNAL,
                    ApplicationConstant.MATCHING_KIND, SubmissionProcessKindEnum.HOT
                )
            )
        ));

    when(submissionProcessingService.processSubmission(any()))
        .thenReturn(Mono.just(
            new MessagingWrapper<>(
                SubmissionMatchDetailEntity.builder().submissionId(3).status("A").build(),
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 3,
                    ApplicationConstant.SUBMISSION_STATUS, SubmissionStatusEnum.A
                )
            )
        ));

    when(legacyPersistenceService.persist(any()))
        .thenReturn(Mono.just(
            new MessagingWrapper<>(3, Map.of(ApplicationConstant.SUBMISSION_ID, 3))
        ));

    when(autoProcessingService.completeProcessing(3))
        .thenReturn(Mono.just(3));

    when(submissionLoadingService.buildMailMessage(any()))
        .thenReturn(Mono.just(TestConstants.EMAIL_REQUEST));

    when(mailService.sendMail(any(EmailRequestDto.class)))
        .thenReturn(Mono.just("OK"));

    service.processedMessages();

    verify(submissionRepository, times(1)).loadProcessedSubmissions();
    verify(autoProcessingService, times(1)).loadMatchingInfo(any());
    verify(submissionProcessingService, times(1)).processSubmission(any());
    verify(legacyPersistenceService, times(1)).persist(any());
    verify(autoProcessingService, times(1)).completeProcessing(3);
    verify(submissionLoadingService, times(1)).buildMailMessage(any());
    verify(mailService, times(1)).sendMail(TestConstants.EMAIL_REQUEST);
  }

  @Test
  @DisplayName("Process staff submitted scheduled job")
  void shouldProcessStaffSubmittedScheduled() {
    when(submissionRepository.loadStaffSubmissions())
        .thenReturn(Flux.just(4));

    when(autoProcessingService.loadMatchingInfo(any()))
        .thenReturn(Mono.just(
            new MessagingWrapper<>(
                4,
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 4,
                    ApplicationConstant.SUBMISSION_STARTER, SubmissionProcessTypeEnum.STAFF,
                    ApplicationConstant.MATCHING_KIND, SubmissionProcessKindEnum.HOT
                )
            )
        ));

    when(submissionProcessingService.processSubmission(any()))
        .thenReturn(Mono.just(
            new MessagingWrapper<>(
                SubmissionMatchDetailEntity.builder().submissionId(4).status("A").build(),
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 4,
                    ApplicationConstant.SUBMISSION_STATUS, SubmissionStatusEnum.A
                )
            )
        ));

    when(legacyPersistenceService.persist(any()))
        .thenReturn(Mono.just(
            new MessagingWrapper<>(4, Map.of(ApplicationConstant.SUBMISSION_ID, 4))
        ));

    when(autoProcessingService.completeProcessing(4))
        .thenReturn(Mono.just(4));

    when(submissionLoadingService.buildMailMessage(any()))
        .thenReturn(Mono.just(TestConstants.EMAIL_REQUEST));

    when(mailService.sendMail(any(EmailRequestDto.class)))
        .thenReturn(Mono.just("OK"));

    service.processStaffSubmitted();

    verify(submissionRepository, times(1)).loadStaffSubmissions();
    verify(autoProcessingService, times(1)).loadMatchingInfo(any());
    verify(submissionProcessingService, times(1)).processSubmission(any());
    verify(legacyPersistenceService, times(1)).persist(any());
    verify(autoProcessingService, times(1)).completeProcessing(4);
    verify(submissionLoadingService, times(1)).buildMailMessage(any());
    verify(mailService, times(1)).sendMail(TestConstants.EMAIL_REQUEST);
  }

  @Test
  @DisplayName("Process approved HOT submission message with legacy persistence")
  void shouldProcessApprovedHotMessage() {
    when(autoProcessingService.loadMatchingInfo(any()))
        .thenReturn(Mono.just(
            new MessagingWrapper<>(
                5,
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 5,
                    ApplicationConstant.SUBMISSION_STARTER, SubmissionProcessTypeEnum.STAFF,
                    ApplicationConstant.MATCHING_KIND, SubmissionProcessKindEnum.HOT
                )
            )
        ));

    when(submissionProcessingService.processSubmission(any()))
        .thenReturn(Mono.just(
            new MessagingWrapper<>(
                SubmissionMatchDetailEntity.builder().submissionId(5).status("A").build(),
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 5,
                    ApplicationConstant.SUBMISSION_STATUS, SubmissionStatusEnum.A
                )
            )
        ));

    when(legacyPersistenceService.persist(any()))
        .thenReturn(Mono.just(
            new MessagingWrapper<>(5, Map.of(ApplicationConstant.SUBMISSION_ID, 5))
        ));

    when(autoProcessingService.completeProcessing(5))
        .thenReturn(Mono.just(5));

    when(submissionLoadingService.buildMailMessage(any()))
        .thenReturn(Mono.just(TestConstants.EMAIL_REQUEST));

    when(mailService.sendMail(any(EmailRequestDto.class)))
        .thenReturn(Mono.just("OK"));

    StepVerifier.create(service.processedMessage(5, SubmissionProcessTypeEnum.STAFF))
        .expectNext("OK")
        .verifyComplete();

    verify(submissionProcessingService, times(1)).processSubmission(any());
    verify(legacyPersistenceService, times(1)).persist(any());
    verify(autoProcessingService, times(1)).completeProcessing(5);
    verify(submissionLoadingService, times(1)).buildMailMessage(any());
    verify(mailService, times(1)).sendMail(TestConstants.EMAIL_REQUEST);
  }

  @Test
  @DisplayName("Process rejected HOT submission message without legacy persistence")
  void shouldProcessRejectedHotMessageWithoutPersist() {
    when(autoProcessingService.loadMatchingInfo(any()))
        .thenReturn(Mono.just(
            new MessagingWrapper<>(
                6,
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 6,
                    ApplicationConstant.SUBMISSION_STARTER, SubmissionProcessTypeEnum.STAFF,
                    ApplicationConstant.MATCHING_KIND, SubmissionProcessKindEnum.HOT
                )
            )
        ));

    when(submissionProcessingService.processSubmission(any()))
        .thenReturn(Mono.just(
            new MessagingWrapper<>(
                SubmissionMatchDetailEntity.builder().submissionId(6).status("R").build(),
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 6,
                    ApplicationConstant.SUBMISSION_STATUS, SubmissionStatusEnum.R
                )
            )
        ));

    when(autoProcessingService.completeProcessing(6))
        .thenReturn(Mono.just(6));

    when(submissionLoadingService.buildMailMessage(any()))
        .thenReturn(Mono.just(TestConstants.EMAIL_REQUEST));

    when(mailService.sendMail(any(EmailRequestDto.class)))
        .thenReturn(Mono.just("OK"));

    StepVerifier.create(service.processedMessage(6, SubmissionProcessTypeEnum.STAFF))
        .expectNext("OK")
        .verifyComplete();

    verify(submissionProcessingService, times(1)).processSubmission(any());
    verifyNoInteractions(legacyPersistenceService);
    verify(autoProcessingService, times(1)).completeProcessing(6);
    verify(submissionLoadingService, times(1)).buildMailMessage(any());
    verify(mailService, times(1)).sendMail(TestConstants.EMAIL_REQUEST);
  }

  @Test
  @DisplayName("Filter out COLD submission message")
  void shouldFilterOutColdMessage() {
    when(autoProcessingService.loadMatchingInfo(any()))
        .thenReturn(Mono.just(
            new MessagingWrapper<>(
                7,
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 7,
                    ApplicationConstant.SUBMISSION_STARTER, SubmissionProcessTypeEnum.STAFF,
                    ApplicationConstant.MATCHING_KIND, SubmissionProcessKindEnum.COLD
                )
            )
        ));

    StepVerifier.create(service.processedMessage(7, SubmissionProcessTypeEnum.STAFF))
        .verifyComplete();

    verifyNoInteractions(submissionProcessingService);
    verifyNoInteractions(legacyPersistenceService);
    verify(autoProcessingService, never()).completeProcessing(any());
    verifyNoInteractions(mailService);
  }

  @Test
  @DisplayName("Propagate TimeoutException when pipeline execution exceeds timeout")
  void shouldPropagateTimeoutExceptionWhenExecutionExceedsTimeout() {
    ForestClientConfiguration timeoutConfig = ForestClientConfiguration.builder()
        .processor(
            ForestClientConfiguration.ProcessorConfiguration.builder()
                .executionTimeout(Duration.ofMillis(100))
                .build()
        )
        .build();

    ProcessorService timeoutService = new ProcessorService(
        submissionRepository,
        submissionLoadingService,
        legacyLoadingService,
        autoProcessingService,
        submissionProcessingService,
        mailService,
        legacyPersistenceService,
        timeoutConfig
    );

    when(autoProcessingService.loadMatchingInfo(any()))
        .thenReturn(Mono.just(
            new MessagingWrapper<>(
                8,
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 8,
                    ApplicationConstant.SUBMISSION_STARTER, SubmissionProcessTypeEnum.STAFF,
                    ApplicationConstant.MATCHING_KIND, SubmissionProcessKindEnum.HOT
                )
            )
        ));

    when(submissionProcessingService.processSubmission(any()))
        .thenReturn(Mono.never());

    StepVerifier.create(timeoutService.processedMessage(8, SubmissionProcessTypeEnum.STAFF))
        .expectError(TimeoutException.class)
        .verify(Duration.ofSeconds(2));
  }

  @Test
  @DisplayName("Propagate error when pipeline step fails")
  void shouldPropagateErrorWhenPipelineStepFails() {
    when(autoProcessingService.loadMatchingInfo(any()))
        .thenReturn(Mono.just(
            new MessagingWrapper<>(
                9,
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 9,
                    ApplicationConstant.SUBMISSION_STARTER, SubmissionProcessTypeEnum.STAFF,
                    ApplicationConstant.MATCHING_KIND, SubmissionProcessKindEnum.HOT
                )
            )
        ));

    when(submissionProcessingService.processSubmission(any()))
        .thenReturn(Mono.error(new RuntimeException("Simulated error")));

    StepVerifier.create(service.processedMessage(9, SubmissionProcessTypeEnum.STAFF))
        .expectError(RuntimeException.class)
        .verify();

    verifyNoInteractions(mailService);
  }

  @Test
  @DisplayName("Fall back to default timeout when configuration is null")
  void shouldFallbackToDefaultTimeoutWhenConfigurationOrProcessorNull() {
    ProcessorService nullConfigService = new ProcessorService(
        submissionRepository,
        submissionLoadingService,
        legacyLoadingService,
        autoProcessingService,
        submissionProcessingService,
        mailService,
        legacyPersistenceService,
        null
    );

    when(autoProcessingService.loadMatchingInfo(any()))
        .thenReturn(Mono.just(
            new MessagingWrapper<>(
                10,
                Map.of(
                    ApplicationConstant.SUBMISSION_ID, 10,
                    ApplicationConstant.SUBMISSION_STARTER, SubmissionProcessTypeEnum.STAFF,
                    ApplicationConstant.MATCHING_KIND, SubmissionProcessKindEnum.COLD
                )
            )
        ));

    StepVerifier.create(nullConfigService.processedMessage(10, SubmissionProcessTypeEnum.STAFF))
        .verifyComplete();

    ForestClientConfiguration emptyConfig = ForestClientConfiguration.builder().build();
    ProcessorService emptyConfigService = new ProcessorService(
        submissionRepository,
        submissionLoadingService,
        legacyLoadingService,
        autoProcessingService,
        submissionProcessingService,
        mailService,
        legacyPersistenceService,
        emptyConfig
    );

    StepVerifier.create(emptyConfigService.processedMessage(10, SubmissionProcessTypeEnum.STAFF))
        .verifyComplete();
  }
}
