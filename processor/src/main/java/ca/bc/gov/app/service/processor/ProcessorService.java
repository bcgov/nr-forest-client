package ca.bc.gov.app.service.processor;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.entity.SubmissionStatusEnum;
import ca.bc.gov.app.repository.SubmissionRepository;
import ca.bc.gov.app.service.client.ClientSubmissionAutoProcessingService;
import ca.bc.gov.app.service.client.ClientSubmissionLoadingService;
import ca.bc.gov.app.service.client.ClientSubmissionMailService;
import ca.bc.gov.app.service.client.ClientSubmissionProcessingService;
import ca.bc.gov.app.service.legacy.LegacyLoadingService;
import ca.bc.gov.app.service.legacy.LegacyPersistenceService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessorService {

  private final SubmissionRepository submissionRepository;
  private final ClientSubmissionLoadingService submissionLoadingService;
  private final LegacyLoadingService legacyLoadingService;
  private final ClientSubmissionAutoProcessingService autoProcessingService;
  private final ClientSubmissionProcessingService submissionProcessingService;
  private final ClientSubmissionMailService mailService;
  private final LegacyPersistenceService legacyPersistenceService;

  @Scheduled(fixedDelay = 30000)
  public void submissionMessages() {
    log.info("Starting submission processing");
    //Load submission
    submissionRepository
        .loadProcessingSubmissions()
        .doOnNext(submission -> log.info("Loading submission for processing {}", submission))
        //Load details
        .flatMap(submissionLoadingService::loadSubmissionDetails)
        .doOnNext(submission -> log.info("Submission loaded, checking for matches {}",
            submission.parameters().get(
                ApplicationConstant.SUBMISSION_ID))
        )
        //Check matches
        .flatMap(legacyLoadingService::matchCheck)
        .doOnNext(
            submission ->
                log.info("Submission matches checked {} {}",
                    submission.parameters().get(ApplicationConstant.SUBMISSION_ID),
                    submission.parameters().get(ApplicationConstant.SUBMISSION_STATUS)
                )
        )
        //Mark as approved or for review
        .flatMap(submission ->
            Mono.just(submission)
                //If is true, means no matches, so auto-approved
                .filter(submissionStatus -> Boolean.TRUE.equals(submissionStatus.parameters().get(
                    ApplicationConstant.SUBMISSION_STATUS))
                )
                .flatMap(autoProcessingService::approved)
                //If filter is false, means there are matches, so send to manual review
                .switchIfEmpty(autoProcessingService.reviewed(submission))
        )
        .doOnNext(submission -> log.info("Submission processed {} as {}",
                submission.payload(),
                submission.parameters().get(ApplicationConstant.SUBMISSION_TYPE)
            )
        )
        //Build mail message and send it
        .flatMap(submissionLoadingService::buildMailMessage)
        .doOnNext(message -> log.info("Email ready to be sent {}", message))
        .flatMap(mailService::sendMail)
        .subscribe();
  }

  @Scheduled(fixedDelay = 30000, initialDelay = 10000)
  public void processedMessages() {
    //Load the submissions that were processed
    submissionRepository
        .loadProcessedSubmissions()
        //Little wrapper to make it easier to pass around
        .map(submission ->
            new MessagingWrapper<>(
                submission,
                Map.of(ApplicationConstant.SUBMISSION_ID, submission)
            )
        )
        .doOnNext(submission -> log.info("Loaded submission for processing {}", submission))
        //Process the submission by loading some information
        .flatMap(submissionProcessingService::processSubmission)
        .doOnNext(submission -> log.info("Submission loaded for post processing {}", submission))
        .flatMap(message ->
            Mono
                .just(message)
                //If the submission is approved or auto approved, go save on oracle
                .filter(submission ->
                    SubmissionStatusEnum.A.equals(
                        submission.parameters()
                            .get(ApplicationConstant.SUBMISSION_STATUS)
                    )
                )
                .flatMap(legacyPersistenceService::persist)
                //If rejected, just send the email
                .defaultIfEmpty(
                    new MessagingWrapper<>(
                        (Integer) message.parameters().get(ApplicationConstant.SUBMISSION_ID),
                        message.parameters()
                    )
                )

                .flatMap(submission ->
                    autoProcessingService
                        .completeProcessing(submission.payload())
                        .map(id -> new MessagingWrapper<>(id, submission.parameters()))
                )

        )
        .doOnNext(submission -> log.info("Submission post processed {}, building message", submission))
        .onErrorContinue((throwable, o) -> log.error("Error processing submission {}", o, throwable))
        .flatMap(submissionLoadingService::buildMailMessage)
        .doOnNext(message -> log.info("Email ready to be sent {}", message))
        .flatMap(mailService::sendMail)
        .subscribe();


  }

}
