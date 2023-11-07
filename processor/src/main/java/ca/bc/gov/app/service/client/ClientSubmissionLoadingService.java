package ca.bc.gov.app.service.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.EmailRequestDto;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.repository.client.SubmissionContactRepository;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import java.util.Map;
import java.util.Objects;
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
public class ClientSubmissionLoadingService {

  private final SubmissionDetailRepository submissionDetailRepository;
  private final SubmissionContactRepository contactRepository;

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
                                    "name", Objects.requireNonNull(message.getHeaders()
                                        .get(ApplicationConstant.FOREST_CLIENT_NAME, String.class)),
                                    "clientNumber",
                                    Objects.requireNonNull(message.getHeaders()
                                        .get(ApplicationConstant.FOREST_CLIENT_NUMBER,
                                            String.class))
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
