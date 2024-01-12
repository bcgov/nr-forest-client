package ca.bc.gov.app.service.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.entity.SubmissionMatchDetailEntity;
import ca.bc.gov.app.entity.SubmissionStatusEnum;
import ca.bc.gov.app.entity.SubmissionTypeCodeEnum;
import ca.bc.gov.app.repository.SubmissionMatchDetailRepository;
import ca.bc.gov.app.repository.SubmissionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * This class is responsible for processing the submission and persisting the data on oracle. It
 * does through a few steps that are interconnected.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClientSubmissionAutoProcessingService {

  private final SubmissionRepository submissionRepository;
  private final SubmissionMatchDetailRepository submissionMatchDetailRepository;

  /**
   * This method is responsible for marking the submission as approved and sending to the nexty
   * step.
   */
  public Mono<MessagingWrapper<Integer>> approved(MessagingWrapper<List<MatcherResult>> message) {
    int submissionId =
        (int) message.parameters()
            .get(ApplicationConstant.SUBMISSION_ID);
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
            .thenReturn(new MessagingWrapper<>(submissionId,Map.of()));
  }

  /**
   * This method is responsible for marking the submission as processed
   */
  public Mono<Integer> completeProcessing(Integer submissionId) {
    return
        submissionMatchDetailRepository
            .findBySubmissionId(submissionId)
            .doOnNext(entity -> entity.setProcessed(true))
            .doOnNext(entity -> entity.setUpdatedAt(LocalDateTime.now()))
            .flatMap(submissionMatchDetailRepository::save)
            .thenReturn(submissionId);
  }

  /**
   * This method is responsible for marking the submission as reviewed
   */
  public Mono<MessagingWrapper<Integer>> reviewed(MessagingWrapper<List<MatcherResult>> message) {
    return
        persistData(
            (int) message.parameters().get(ApplicationConstant.SUBMISSION_ID),
            SubmissionTypeCodeEnum.RNC
        )
            .doOnNext(id -> log.info("Request {} was put into review", id))
            .flatMap(this::loadFirstOrNew)
            .doOnNext(entity -> entity.setMatchers(
                    message
                        .payload()
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
                    message.parameters().get(ApplicationConstant.SUBMISSION_ID)
                )
            )
            .map(entity ->
                new MessagingWrapper<>(
                    entity.getSubmissionId(),
                    message.parameters()
                )
                    .withParameter(ApplicationConstant.SUBMISSION_STATUS, SubmissionStatusEnum.N)
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
