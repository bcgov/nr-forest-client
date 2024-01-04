package ca.bc.gov.app.service.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.EmailRequestDto;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.repository.SubmissionContactRepository;
import ca.bc.gov.app.repository.SubmissionDetailRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


/**
 * This class is responsible for loading the submission details and submission contact details
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClientSubmissionLoadingService {

  private final SubmissionDetailRepository submissionDetailRepository;
  private final SubmissionContactRepository contactRepository;


  /**
   * Load the submission details to be processed later on
   */
  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_LIST_CHANNEL,
      outputChannel = ApplicationConstant.MATCH_CHECKING_CHANNEL,
      async = "true"
  )
  public Mono<Message<SubmissionInformationDto>> loadSubmissionDetails(Integer submissionId) {

    return
        submissionDetailRepository
            .findBySubmissionId(submissionId)
            .doOnNext(submission -> log.info("Loaded submission details {}", submission))
            //Grab what we need for the match part
            .map(details -> new SubmissionInformationDto(
                    submissionId,
                    details.getOrganizationName(),
                    details.getBirthdate(),
                    details.getIncorporationNumber(),
                    details.getGoodStandingInd(),
                    details.getClientTypeCode()
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


  /**
   * Build the email request dto to be sent to the email service
   */
  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_MAIL_BUILD_CHANNEL,
      outputChannel = ApplicationConstant.SUBMISSION_MAIL_CHANNEL,
      async = "true"
  )
  public Mono<Message<EmailRequestDto>> sendNotification(Message<Integer> message) {

    return
        submissionDetailRepository
            .findBySubmissionId(message.getPayload())
            .flatMap(details ->
                contactRepository
                    .findFirstBySubmissionId(message.getPayload())
                    .map(submissionContact ->
                        new EmailRequestDto(
                            details.getIncorporationNumber(),
                            details.getOrganizationName(),
                            submissionContact.getUserId(),
                            submissionContact.getFirstName(),
                            submissionContact.getEmailAddress(),
                            "approval",
                            "Success",
                            Map.of(
                                "userName", submissionContact.getFirstName(),
                                "business", Map.of(
                                    "name", details.getOrganizationName(),
                                    "clientNumber", details.getClientNumber()
                                )
                            )
                        )
                    )
            )
            .map(emailRequestDto ->
                MessageBuilder
                    .withPayload(emailRequestDto)
                    .copyHeaders(message.getHeaders())
                    .build()
            );
  }

}
