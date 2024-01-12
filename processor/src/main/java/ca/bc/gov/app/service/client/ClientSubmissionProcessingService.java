package ca.bc.gov.app.service.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.entity.SubmissionMatchDetailEntity;
import ca.bc.gov.app.repository.SubmissionDetailRepository;
import ca.bc.gov.app.repository.SubmissionMatchDetailRepository;
import ca.bc.gov.app.repository.SubmissionRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientSubmissionProcessingService {

  private final SubmissionDetailRepository submissionDetailRepository;
  private final SubmissionRepository submissionRepository;
  private final SubmissionMatchDetailRepository submissionMatchDetailRepository;

  /**
   * This method will process the submission and send the notification to the user.
   */
  public Mono<MessagingWrapper<SubmissionMatchDetailEntity>> processSubmission(
      MessagingWrapper<Integer> submissionMessage
  ) {
    Integer submissionId = submissionMessage.payload();

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
                                new MessagingWrapper<>(
                                    matching,
                                    submissionMessage.parameters()
                                )
                                    .withParameter(ApplicationConstant.SUBMISSION_ID, submissionId)
                                    .withParameter(ApplicationConstant.SUBMISSION_STATUS,
                                        event.getSubmissionStatus())
                                    .withParameter(ApplicationConstant.SUBMISSION_CLIENTID,
                                        Optional.ofNullable(details.getClientNumber())
                                            .orElse(StringUtils.EMPTY))
                                    .withParameter(ApplicationConstant.SUBMISSION_NAME,
                                        details.getOrganizationName())
                                    .withParameter(ApplicationConstant.MATCHING_REASON,
                                        matching.getMatchingMessage())
                                    .withParameter(ApplicationConstant.CLIENT_TYPE_CODE,
                                        details.getClientTypeCode())
                            )
                    )
            );
  }

}
