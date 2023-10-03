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
    persistData(submissionId, SubmissionTypeCodeEnum.AAC);
    log.info("Request {} was approved",submissionId);


    submissionMatchDetailRepository
        .save(
            SubmissionMatchDetailEntity
                .builder()
                .matchers(Map.of())
                .submissionId(
                    message.getHeaders().get(ApplicationConstant.SUBMISSION_ID, Integer.class)
                )
                .status("Y")
                .createdBy("AUTO-PROCESSOR")
                .updatedAt(LocalDateTime.now())
                .build()
        )
        .subscribe(entity -> log.info(
                "Added matches for submission {} {}",
                entity.getSubmissionId(),
                entity.getMatchingField()
            )
        );



    return Mono.just(MessageBuilder.withPayload(submissionId).build());
  }


  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_COMPLETION_CHANNEL,
      outputChannel = ApplicationConstant.SUBMISSION_MAIL_CHANNEL,
      async = "true"
  )
  public Mono<Message<EmailRequestDto>> completeProcessing(Message<EmailRequestDto> message) {
    return
      submissionMatchDetailRepository
          .findBySubmissionId(message.getHeaders().get(ApplicationConstant.SUBMISSION_ID, Integer.class))
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
    );

    log.info("Request {} was put into review",
        message.getHeaders().get(ApplicationConstant.SUBMISSION_ID, Integer.class));

    submissionMatchDetailRepository
        .save(
            SubmissionMatchDetailEntity
                .builder()
                .matchers(message
                    .getPayload()
                    .stream()
                    .collect(Collectors.toMap(MatcherResult::fieldName, MatcherResult::value))
                )
                .submissionId(
                    message.getHeaders().get(ApplicationConstant.SUBMISSION_ID, Integer.class)
                )
                .createdBy("AUTO-PROCESSOR")
                .updatedAt(LocalDateTime.now())
                .build()
        )
        .subscribe(entity -> log.info(
                "Added matches for submission {} {}",
                entity.getSubmissionId(),
                entity.getMatchingField()
            )
        );

    log.info("Request {} was put into review",
        message.getHeaders().get(ApplicationConstant.SUBMISSION_ID, Integer.class));
  }

  private void persistData(Integer submissionId, SubmissionTypeCodeEnum typeCode) {
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
        .doOnNext(entity -> entity.setUpdatedBy("AUTO-PROCESSOR"))
        .doOnNext(entity -> entity.setUpdatedAt(LocalDateTime.now()))
        .flatMap(submissionRepository::save)
        .subscribe(entity -> log.info(
                "Updated submission {} with typeCode {}",
                entity.getSubmissionId(),
                entity.getSubmissionType().getDescription()
            )
        );
  }

}
