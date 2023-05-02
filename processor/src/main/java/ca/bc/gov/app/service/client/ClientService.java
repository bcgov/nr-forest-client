package ca.bc.gov.app.service.client;

import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
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
      inputChannel = "submissionListChannel",
      outputChannel = "matchCheckingChannel",
      async = "true"
  )
  public Mono<Message<SubmissionInformationDto>> loadSubmissionDetails(Integer submissionId) {

    return
        //Load the submission data (Maybe we won't need it
        //TODO: check if we will need the submission data
        submissionRepository
            .findById(submissionId)
            .doOnNext(entity -> log.info("Loading submission data {}", entity))

            //Load de the details
            .flatMap(submission -> submissionDetailRepository
                .findBySubmissionId(submissionId)
                //Grab what we need for the match part
                .map(details -> new SubmissionInformationDto(
                        details.getDisplayName(),
                        details.getIncorporationNumber(),
                        null
                    )
                )
            )

            //Build a message with our dto and pass the submission Id as header
            .map(event ->
                MessageBuilder
                    .withPayload(event)
                    .setHeader("submission-id", submissionId)
                    .build()
            );
  }
}
