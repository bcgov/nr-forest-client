package ca.bc.gov.app.service.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.entity.client.SubmissionStatusEnum;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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
public class ClientService {

  private final SubmissionRepository submissionRepository;
  private final SubmissionDetailRepository submissionDetailRepository;

  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_LIST_CHANNEL,
      outputChannel = ApplicationConstant.MATCH_CHECKING_CHANNEL,
      async = "true"
  )
  public Mono<Message<SubmissionInformationDto>> loadSubmissionDetails(Integer submissionId) {

    return
        submissionDetailRepository
            .findBySubmissionId(submissionId)
            //Grab what we need for the match part
            .map(details -> new SubmissionInformationDto(
                    details.getOrganizationName(),
                    details.getIncorporationNumber(),
                    details.getGoodStandingInd()
                )
            )

            //Build a message with our dto and pass the submission Id as header
            .map(event ->
                MessageBuilder
                    .withPayload(event)
                    .setHeader(ApplicationConstant.SUBMISSION_ID, submissionId)
                    .build()
            );
  }

  @ServiceActivator(inputChannel = ApplicationConstant.AUTO_APPROVE_CHANNEL)
  public void approved(Message<List<MatcherResult>> message) {
    persistData(
        message.getHeaders().get(ApplicationConstant.SUBMISSION_ID, Integer.class),
        SubmissionStatusEnum.A
    );
    log.info("Request {} was approved",
        message.getHeaders().get(ApplicationConstant.SUBMISSION_ID, Integer.class));
  }

  @ServiceActivator(inputChannel = ApplicationConstant.REVIEW_CHANNEL)
  public void reviewed(Message<List<MatcherResult>> message) {
    persistData(
        message.getHeaders().get(ApplicationConstant.SUBMISSION_ID, Integer.class),
        SubmissionStatusEnum.R
    );
    log.info("Request {} was put into review",
        message.getHeaders().get(ApplicationConstant.SUBMISSION_ID, Integer.class));
  }

  private void persistData(Integer submissionId, SubmissionStatusEnum status) {
    submissionRepository
        .findById(submissionId)
        .map(entity -> entity.withSubmissionStatus(status))
        .doOnNext(
            entity -> entity.setUpdatedBy(UUID.randomUUID().toString())) //TODO: Need to update ?
        .doOnNext(
            entity -> entity.setCreatedBy(UUID.randomUUID().toString())) //TODO: Need to update ?
        .doOnNext(entity -> entity.setUpdatedAt(LocalDateTime.now()))
        .flatMap(submissionRepository::save)
        .subscribe(entity -> log.info(
                "Updated submission {} with status {}",
                entity.getSubmissionId(),
                entity.getSubmissionStatus()
            )
        );
  }


}

