package ca.bc.gov.app.service.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.EmailRequestDto;
import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.entity.client.SubmissionMatchDetailEntity;
import ca.bc.gov.app.entity.client.SubmissionStatusEnum;
import ca.bc.gov.app.entity.client.SubmissionTypeCodeEnum;
import ca.bc.gov.app.repository.client.SubmissionMatchDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
public class ClientSubmissionAutoProcessingService {

  private final SubmissionRepository submissionRepository;
  private final SubmissionMatchDetailRepository submissionMatchDetailRepository;

  @ServiceActivator(
      inputChannel = ApplicationConstant.AUTO_APPROVE_CHANNEL,
      outputChannel = ApplicationConstant.SUBMISSION_POSTPROCESSOR_CHANNEL,
      async = "true"
  )
  public Mono<Message<Integer>> approved(Message<List<MatcherResult>> message) {
    Integer submissionId = message.getHeaders()
        .get(ApplicationConstant.SUBMISSION_ID, Integer.class);
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
            .thenReturn(MessageBuilder.withPayload(submissionId).build());
  }


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

  //TODO: must send an email to admin team
  @ServiceActivator(inputChannel = ApplicationConstant.REVIEW_CHANNEL)
  public void reviewed(Message<List<MatcherResult>> message) {
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
        .subscribe(entity -> log.info(
                "Added matches for submission {} {}",
                entity.getSubmissionId(),
                entity.getMatchingField()
            )
        );

    log.info("Request {} was put into review",
        message.getHeaders().get(ApplicationConstant.SUBMISSION_ID, Integer.class));
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
