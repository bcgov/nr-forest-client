package ca.bc.gov.app.service.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.dto.SubmissionProcessKindEnum;
import ca.bc.gov.app.entity.SubmissionMatchDetailEntity;
import ca.bc.gov.app.entity.SubmissionStatusEnum;
import ca.bc.gov.app.entity.SubmissionTypeCodeEnum;
import ca.bc.gov.app.repository.SubmissionMatchDetailRepository;
import ca.bc.gov.app.repository.SubmissionRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
   * This method is responsible for marking the submission as approved and sending it to the next
   * step.
   */
  public Mono<MessagingWrapper<Integer>> approved(
      MessagingWrapper<List<MatcherResult>> message) {

    int submissionId =
        (int) message.parameters().get(ApplicationConstant.SUBMISSION_ID);

    return persistData(submissionId, SubmissionTypeCodeEnum.AAC)
        .doOnNext(id -> log.info("Request {} was approved", id))
        .flatMap(this::loadFirstOrNew)
        .doOnNext(entity -> entity.setStatus("Y"))
        // Preserve existing MATCHING_INFO when approving; clear other matcher entries
        .doOnNext(entity -> {
          Map<String, Object> existing =
              entity.getMatchers() == null
                  ? new HashMap<>()
                  : new HashMap<>(entity.getMatchers());

          Map<String, Object> keep = new HashMap<>();
          if (existing.containsKey(ApplicationConstant.MATCHING_INFO)) {
            keep.put(
                ApplicationConstant.MATCHING_INFO,
                existing.get(ApplicationConstant.MATCHING_INFO)
            );
          }
          entity.setMatchers(keep);
        })
        .flatMap(submissionMatchDetailRepository::save)
        .thenReturn(new MessagingWrapper<>(submissionId, Map.of()));
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
            .doOnNext(entity -> entity.setCreatedBy(StringUtils.isBlank(entity.getCreatedBy())
                ? ApplicationConstant.PROCESSOR_USER_NAME : entity.getCreatedBy()))
            .flatMap(submissionMatchDetailRepository::save)
            .thenReturn(submissionId);
  }

  /**
   * This method is responsible for marking the submission as reviewed
   */
  public Mono<MessagingWrapper<Integer>> reviewed(MessagingWrapper<List<MatcherResult>> message) {
    int submissionId = (int) message.parameters().get(ApplicationConstant.SUBMISSION_ID);

    return persistData(submissionId, SubmissionTypeCodeEnum.RNC)
        .doOnNext(id -> log.info("Request {} was put into review", id))
        .flatMap(this::loadFirstOrNew)
        .doOnNext(entity -> updateEntityMatchers(entity, message))
        .flatMap(submissionMatchDetailRepository::save)
        .doOnNext(entity -> log.info(
                "Added matches for submission {} {}",
                entity.getSubmissionId(),
                entity.getMatchingField()
            )
        )
        .map(entity -> createMessagingWrapper(entity, message));
  }

  /**
   * This method is used to load matching information for a given submission. It atomically claims
   * the submission for processing by updating its processing timestamp, or by atomically creating
   * and claiming the matching detail row if missing. If the claim succeeds, matching information
   * is loaded and added to the message parameters under 'MATCHING_INFO', and the message is marked
   * HOT. If another process has already claimed or completed the submission, the message is marked
   * COLD.
   *
   * @param message A MessagingWrapper object that contains the submission ID in its parameters.
   * @return A Mono of MessagingWrapper with MATCHING_KIND set to HOT if claimed, or COLD
   *         otherwise.
   */
  public <T> Mono<MessagingWrapper<T>> loadMatchingInfo(MessagingWrapper<T> message) {
    int submissionId = (int) message.parameters().get(ApplicationConstant.SUBMISSION_ID);

    return
        submissionMatchDetailRepository
            .claimSubmissionForProcessing(submissionId)
            .flatMap(updatedCount -> {
              if (updatedCount > 0) {
                return loadClaimedMatchingInfo(message, submissionId);
              }
              return submissionMatchDetailRepository
                  .createAndClaimIfMissing(submissionId)
                  .flatMap(insertedCount -> {
                    if (insertedCount > 0) {
                      log.info(
                          "Created missing matching detail and claimed submission {}",
                          submissionId
                      );
                      return loadClaimedMatchingInfo(message, submissionId);
                    }
                    log.info(
                        "Submission {} is already being processed or completed, marking as COLD",
                        submissionId
                    );
                    return Mono.just(
                        message.withParameter(
                            ApplicationConstant.MATCHING_KIND,
                            SubmissionProcessKindEnum.COLD
                        )
                    );
                  })
                  .onErrorResume(e -> {
                    log.warn(
                        "Failed to claim missing detail for submission {}, marking COLD: {}",
                        submissionId,
                        e.getMessage()
                    );
                    return Mono.just(
                        message.withParameter(
                            ApplicationConstant.MATCHING_KIND,
                            SubmissionProcessKindEnum.COLD
                        )
                    );
                  });
            })
            .defaultIfEmpty(
                message.withParameter(
                    ApplicationConstant.MATCHING_KIND,
                    SubmissionProcessKindEnum.COLD
                )
            );
  }

  private <T> Mono<MessagingWrapper<T>> loadClaimedMatchingInfo(
      MessagingWrapper<T> message,
      int submissionId
  ) {
    return submissionMatchDetailRepository
        .findBySubmissionId(submissionId)
        .doOnNext(entity -> log.info(
            "Claimed and loaded submission {} with matching info {}",
            entity.getSubmissionId(),
            entity.getMatchingField()
        ))
        .map(entity ->
            message
                .withParameter(
                    ApplicationConstant.MATCHING_INFO,
                    entity.getMatchers() != null
                        ? entity.getMatchers().get(ApplicationConstant.MATCHING_INFO)
                        : null
                )
                .withParameter(
                    ApplicationConstant.MATCHING_KIND,
                    SubmissionProcessKindEnum.HOT
                )
        )
        .defaultIfEmpty(
            message.withParameter(
                ApplicationConstant.MATCHING_KIND,
                SubmissionProcessKindEnum.HOT
            )
        );
  }

  private void updateEntityMatchers(
      SubmissionMatchDetailEntity entity,
      MessagingWrapper<List<MatcherResult>> message) {

    Map<String, Object> existing =
        entity.getMatchers() == null
            ? new HashMap<>()
            : new HashMap<>(entity.getMatchers());

    Map<String, Object> incoming =
        message.payload()
            .stream()
            .filter(MatcherResult::hasMatch)
            .collect(Collectors.toMap(
                MatcherResult::fieldName,
                MatcherResult::value
            ));

    // Merge incoming into existing but do NOT overwrite MATCHING_INFO
    incoming.forEach((k, v) -> {
      if (!ApplicationConstant.MATCHING_INFO.equals(k)) {
        existing.put(k, v);
      }
    });

    entity.setMatchers(existing);
  }

  private MessagingWrapper<Integer> createMessagingWrapper(SubmissionMatchDetailEntity entity,
      MessagingWrapper<List<MatcherResult>> message) {
    return new MessagingWrapper<>(entity.getSubmissionId(), message.parameters())
        .withParameter(ApplicationConstant.SUBMISSION_STATUS, SubmissionStatusEnum.N);
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
