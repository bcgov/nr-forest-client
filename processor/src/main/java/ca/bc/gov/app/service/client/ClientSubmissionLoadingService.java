package ca.bc.gov.app.service.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
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

}
