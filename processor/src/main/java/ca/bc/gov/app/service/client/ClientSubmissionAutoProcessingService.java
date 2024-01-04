package ca.bc.gov.app.service.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.EmailRequestDto;
import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.entity.SubmissionMatchDetailEntity;
import ca.bc.gov.app.entity.SubmissionStatusEnum;
import ca.bc.gov.app.entity.SubmissionTypeCodeEnum;
import ca.bc.gov.app.repository.SubmissionMatchDetailRepository;
import ca.bc.gov.app.repository.SubmissionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * This class is responsible for processing the submission and persisting the data on oracle.
 * It does through a few steps that are interconnected.
 */
public class ClientSubmissionAutoProcessingService {

  private final SubmissionRepository submissionRepository;
  private final SubmissionMatchDetailRepository submissionMatchDetailRepository;

  /**
   * This method is responsible for marking the submission as approved
   * and sending to the nexty step.
   */
  @ServiceActivator(
      inputChannel = ApplicationConstant.AUTO_APPROVE_CHANNEL,
      outputChannel = ApplicationConstant.SUBMISSION_POSTPROCESSOR_CHANNEL,
      async = "true"
  )
  public Mono<Message<Integer>> approved(Message<List<MatcherResult>> message) {
    int submissionId =
        Objects.requireNonNull(
            message.getHeaders()
                .get(ApplicationConstant.SUBMISSION_ID, Integer.class)
        );
    return
        persistData(submissionId, SubmissionTypeCodeEnum.AAC)
            .doOnNext(id -> log.info("Request {} was approved", id))
            .flatMap(this::loadFirstOrNew)
            .doOnNext(entity -> entity.setStatus("Y"))
            .doOnNext(entity -> entity.setMatchers(Map.of()))
            .flatMap(submissionMatchDetailRepository::save)
            .doOnNext(entity -> log.info(
                    "Added matches for submission {} {}",
                    entity.getSubmissionId(),
                    entity.getMatchingField()
                )
            )
            .thenReturn(MessageBuilder
                .withPayload(submissionId)
                .setHeader(ApplicationConstant.SUBMISSION_TYPE, SubmissionTypeCodeEnum.AAC)
                .build()
            );
  }


  /**
   * This method is responsible for marking the submission as processed
   */
  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_COMPLETION_CHANNEL,
      outputChannel = ApplicationConstant.SUBMISSION_MAIL_CHANNEL,
      async = "true"
  )
  public Mono<Message<EmailRequestDto>> completeProcessing(Message<EmailRequestDto> message) {
    return
        submissionMatchDetailRepository
            .findBySubmissionId(
                message.getHeaders().get(ApplicationConstant.SUBMISSION_ID, Integer.class))
            .doOnNext(entity -> entity.setProcessed(true))
            .doOnNext(entity -> entity.setUpdatedAt(LocalDateTime.now()))
            .flatMap(submissionMatchDetailRepository::save)
            .thenReturn(message);
  }

  /**
   * This method is responsible for marking the submission as reviewed
   */
  @ServiceActivator(
      inputChannel = ApplicationConstant.REVIEW_CHANNEL,
      outputChannel = ApplicationConstant.SUBMISSION_MAIL_BUILD_CHANNEL,
      async = "true"
  )
  public Mono<Message<Integer>> reviewed(Message<List<MatcherResult>> message) {
    return
        persistData(
            message.getHeaders().get(ApplicationConstant.SUBMISSION_ID, Integer.class),
            SubmissionTypeCodeEnum.RNC
        )
            .doOnNext(id -> log.info("Request {} was put into review", id))
            .flatMap(this::loadFirstOrNew)
            .doOnNext(entity -> entity.setMatchers(
                    message
                        .getPayload()
                        .stream()
                        .collect(Collectors.toMap(MatcherResult::fieldName, MatcherResult::value))
                )
            )
            .flatMap(submissionMatchDetailRepository::save)
            .doOnNext(entity -> log.info(
                    "Added matches for submission {} {}",
                    entity.getSubmissionId(),
                    entity.getMatchingField()
                )
            )
            .doOnNext(entity ->
                log.info("Request {} was put into review",
                    message.getHeaders().get(ApplicationConstant.SUBMISSION_ID, Integer.class)
                )
            )
            .map(entity -> MessageBuilder
                .withPayload(entity.getSubmissionId())
                .copyHeaders(message.getHeaders())
                .setHeader(ApplicationConstant.SUBMISSION_TYPE, SubmissionTypeCodeEnum.RNC)
                .build()
            );
  }

  private Mono<Integer> persistData(Integer submissionId, SubmissionTypeCodeEnum typeCode) {
    return
        submissionRepository
            .findById(submissionId)
            .doOnNext(entity -> entity.setSubmissionType(typeCode))
            .doOnNext(entity -> entity
                .setSubmissionStatus(
                    SubmissionTypeCodeEnum.AAC.equals(typeCode)
                        ? SubmissionStatusEnum.A
                        : SubmissionStatusEnum.N
                )
            )
            .doOnNext(entity -> entity.setUpdatedBy(ApplicationConstant.PROCESSOR_USER_NAME))
            .doOnNext(entity -> entity.setUpdatedAt(LocalDateTime.now()))
            .flatMap(submissionRepository::save)
            .doOnNext(entity -> log.info(
                    "Updated submission {} with typeCode {}",
                    entity.getSubmissionId(),
                    entity.getSubmissionType().getDescription()
                )
            )
            .thenReturn(submissionId);
  }

  private Mono<SubmissionMatchDetailEntity> loadFirstOrNew(Integer submissionId) {
    return
        submissionMatchDetailRepository
            .findAllBySubmissionId(submissionId)
            .next()
            .defaultIfEmpty(
                SubmissionMatchDetailEntity
                    .builder()
                    .submissionId(submissionId)
                    .createdBy(ApplicationConstant.PROCESSOR_USER_NAME)
                    .updatedAt(LocalDateTime.now())
                    .build()
            );
  }

}
