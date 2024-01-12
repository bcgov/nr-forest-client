package ca.bc.gov.app.service.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.EmailRequestDto;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.entity.SubmissionStatusEnum;
import ca.bc.gov.app.repository.SubmissionContactRepository;
import ca.bc.gov.app.repository.SubmissionDetailRepository;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  public Mono<MessagingWrapper<SubmissionInformationDto>> loadSubmissionDetails(
      Integer submissionId) {

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
            .map(event -> new MessagingWrapper<>(
                    event,
                    Map.of(ApplicationConstant.SUBMISSION_ID, submissionId)
                )
            );
  }

  public Mono<EmailRequestDto> buildMailMessage(
      MessagingWrapper<Integer> message
  ) {

    if (message
            .parameters()
            .get(ApplicationConstant.SUBMISSION_STATUS) == null
    ) {
      return Mono.empty();
    }

    //TODO: read from config
    String clientAdminTeamEmail = "clientadminteamemail@email.ca";
    return
        submissionDetailRepository
            .findBySubmissionId(message.payload())
            .doOnNext(
                submission -> log.info("Loaded submission details for mail purpose {}", submission))
            .flatMap(details ->
                contactRepository
                    .findFirstBySubmissionId(message.payload())
                    .doOnNext(submissionContact -> log.info(
                        "Loaded submission contact details for mail purpose {}", submissionContact))
                    .map(submissionContact ->
                        new EmailRequestDto(
                            details.getIncorporationNumber(),
                            details.getOrganizationName(),
                            submissionContact.getUserId(),
                            submissionContact.getFirstName(),
                            isClientAdminEmail(message)
                                ?
                                clientAdminTeamEmail
                                :
                                    submissionContact.getEmailAddress(),
                            getTemplate(message),
                            getSubject(message, details.getOrganizationName()),
                            getParameter(
                                message,
                                submissionContact.getFirstName(),
                                details.getOrganizationName(),
                                Objects.toString(details.getClientNumber(), ""),
                                String.valueOf(
                                    message.parameters().get(ApplicationConstant.MATCHING_REASON)),
                                message.payload()
                            )
                        )
                    )
            );
  }

  private boolean isClientAdminEmail(MessagingWrapper<Integer> message) {
    return SubmissionStatusEnum.N.equals(
        message
            .parameters()
            .get(ApplicationConstant.SUBMISSION_STATUS)
    );
  }

  private String getTemplate(MessagingWrapper<Integer> message) {
    return switch ((SubmissionStatusEnum) message.parameters()
        .get(ApplicationConstant.SUBMISSION_STATUS)) {
      case A -> "approval";
      case R -> "rejection";
      default -> "revision";
    };
  }

  private String getSubject(
      MessagingWrapper<Integer> message,
      String businessName
  ) {
    return switch ((SubmissionStatusEnum) message.parameters()
        .get(ApplicationConstant.SUBMISSION_STATUS)) {
      case A -> "Client number submission for " + businessName + " was approved";
      case R -> "Client number submission for " + businessName + " was rejected";
      default -> businessName + " submission requires review";
    };
  }

  private Map<String, Object> getParameter(
      MessagingWrapper<Integer> message,
      String username,
      String businessName,
      String clientNumber,
      String reason,
      Integer submissionId
  ) {
    return switch ((SubmissionStatusEnum) message.parameters()
        .get(ApplicationConstant.SUBMISSION_STATUS)) {
      case A -> approvalParameters(username, businessName, clientNumber);
      case R -> rejectionParameters(username, businessName, clientNumber, reason);
      default -> revisionParameters(username, submissionId);
    };
  }

  private Map<String, Object> revisionParameters(
      String username,
      Integer submissionId
  ) {
    return Map.of(
        "userName", username,
        "submission", submissionId
    );
  }

  private Map<String, Object> approvalParameters(
      String username,
      String businessName,
      String clientNumber
  ) {
    return Map.of(
        "userName", username,
        "business", Map.of(
            "name", businessName,
            "clientNumber", clientNumber
        )
    );
  }

  private Map<String, Object> rejectionParameters(
      String username,
      String businessName,
      String clientNumber,
      String reason
  ) {
    return Map.of(
        "userName", username,
        "reason", reason,
        "business", Map.of(
            "name", businessName,
            "clientNumber", clientNumber
        )
    );
  }

}
