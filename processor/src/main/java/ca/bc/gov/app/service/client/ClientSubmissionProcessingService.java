package ca.bc.gov.app.service.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.EmailRequestDto;
import ca.bc.gov.app.entity.client.SubmissionMatchDetailEntity;
import ca.bc.gov.app.entity.client.SubmissionStatusEnum;
import ca.bc.gov.app.repository.client.SubmissionContactRepository;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionMatchDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import ca.bc.gov.app.util.ProcessorUtil;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientSubmissionProcessingService {

  private final SubmissionDetailRepository submissionDetailRepository;
  private final SubmissionRepository submissionRepository;
  private final SubmissionMatchDetailRepository submissionMatchDetailRepository;
  private final SubmissionContactRepository contactRepository;

  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_POSTPROCESSOR_CHANNEL,
      outputChannel = ApplicationConstant.NOTIFICATION_PROCESSING_CHANNEL,
      async = "true"
  )
  public Mono<Message<SubmissionMatchDetailEntity>> processSubmission(Integer submissionId) {
    return
        submissionRepository
            .findById(submissionId)
            .doOnNext(submission -> log.info("Loaded submission {}", submission))
            .flatMap(event ->
                submissionDetailRepository
                    .findBySubmissionId(submissionId)
                    .flatMap(details ->
                        submissionMatchDetailRepository
                            .findBySubmissionId(submissionId)
                            .defaultIfEmpty(SubmissionMatchDetailEntity.builder().build())
                            .map(matching ->
                                MessageBuilder
                                    .withPayload(matching)
                                    .setHeader(ApplicationConstant.SUBMISSION_ID, submissionId)
                                    .setHeader(ApplicationConstant.SUBMISSION_STATUS,
                                        event.getSubmissionStatus())
                                    .setHeader(ApplicationConstant.SUBMISSION_CLIENTID,
                                        Optional.ofNullable(details.getClientNumber())
                                            .orElse(StringUtils.EMPTY))
                                    .setHeader(ApplicationConstant.SUBMISSION_NAME,
                                        details.getOrganizationName())

                                    .build()
                            )
                    )
            );
  }

  @ServiceActivator(
      inputChannel = ApplicationConstant.NOTIFICATION_PROCESSING_CHANNEL,
      outputChannel = ApplicationConstant.FORWARD_CHANNEL,
      async = "true"
  )
  public Mono<Message<?>> notificationProcessing(Message<SubmissionMatchDetailEntity> message) {

    SubmissionStatusEnum status = ProcessorUtil.readHeader(message, ApplicationConstant.SUBMISSION_STATUS,
        SubmissionStatusEnum.class)
        .orElse(SubmissionStatusEnum.R);

    Integer submissionId = ProcessorUtil.readHeader(message, ApplicationConstant.SUBMISSION_ID, Integer.class).orElse(0);

    if (SubmissionStatusEnum.A.equals(status)) {

      log.info("Submission {} was approved", submissionId);

      return Mono
          .just(
              MessageBuilder
                  .withPayload(submissionId)
                  .setReplyChannelName(ApplicationConstant.SUBMISSION_LEGACY_CHANNEL)
                  .setHeader("output-channel", ApplicationConstant.SUBMISSION_LEGACY_CHANNEL)
                  .setHeader(MessageHeaders.REPLY_CHANNEL, ApplicationConstant.SUBMISSION_LEGACY_CHANNEL)
                  .build()
          );

    }

    log.info("Submission {} was rejected", submissionId);

    return
      submissionMatchDetailRepository
          .findBySubmissionId(submissionId)
          .map(SubmissionMatchDetailEntity::getMatchingMessage)
          .flatMap(matchingReason ->
              submissionDetailRepository
                .findBySubmissionId(submissionId)
                .flatMap(submissionDetails ->
                    contactRepository
                        .findFirstBySubmissionId(submissionId)
                        .map(submissionContact ->
                              new EmailRequestDto(
                                  submissionDetails.getIncorporationNumber(),
                                  submissionDetails.getOrganizationName(),
                                  submissionContact.getUserId(),
                                  submissionContact.getFirstName(),
                                  submissionContact.getEmailAddress(),
                                  "rejection",
                                  "Failure",
                                  Map.of(
                                      "userName", submissionContact.getFirstName(),
                                      "name", submissionDetails.getOrganizationName(),
                                      "reason", matchingReason
                                  )
                              )
                        )
                )
          )
          .map(emailRequestDto ->
              MessageBuilder
                  .withPayload(emailRequestDto)
                  .setReplyChannelName(ApplicationConstant.SUBMISSION_COMPLETION_CHANNEL)
                  .setHeader(ApplicationConstant.SUBMISSION_ID,submissionId)
                  .setHeader("output-channel", ApplicationConstant.SUBMISSION_COMPLETION_CHANNEL)
                  .setHeader(MessageHeaders.REPLY_CHANNEL, ApplicationConstant.SUBMISSION_COMPLETION_CHANNEL)
                  .build()
          );
  }

}
