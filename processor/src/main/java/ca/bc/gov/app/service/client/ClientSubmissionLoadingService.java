package ca.bc.gov.app.service.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.DistrictDto;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
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
  private final WebClient forestClientApi;

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

    return
        submissionDetailRepository
            .findBySubmissionId(message.payload())
            .doOnNext(
                submission -> log.info("Loaded submission details for mail purpose {}", submission)
            )
            .flatMap(details ->
                contactRepository
                    .findFirstBySubmissionId(message.payload())
                    .doOnNext(submissionContact -> log.info(
                        "Loaded submission contact details for mail purpose {}", submissionContact)
                    )
                    // Reads the district information from the forest client district endpoint if is a client admin email
                    .flatMap(submissionContact ->
                        Mono
                            .just(isClientAdminEmail(message))
                            .filter(Boolean::booleanValue)
                            .flatMap(isAdmin ->
                                forestClientApi
                                    .get()
                                    .uri("/api/clients/districts/{districtCode}", details.getDistrictCode())
                                    .exchangeToMono(clientResponse -> clientResponse.bodyToMono(
                                        DistrictDto.class)
                                    )
                                    .doOnNext(district -> log.info(
                                                                "Loaded district details {} {}", 
                                                                district.code(),
                                                                district.description()))
                                    .map(DistrictDto::emails)
                            )
                            .defaultIfEmpty(submissionContact.getEmailAddress())
                            .map(mail -> Pair.of(submissionContact, mail))
                    )
                    .map(submissionContactPair ->
                        new EmailRequestDto(
                            details.getIncorporationNumber(),
                            details.getOrganizationName(),
                            submissionContactPair.getLeft().getUserId(),
                            submissionContactPair.getLeft().getFirstName(),
                            submissionContactPair.getRight(),
                            getTemplate(message),
                            getSubject(message, details.getOrganizationName()),
                            getParameter(
                                message,
                                submissionContactPair.getLeft().getFirstName(),
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
      case A -> "Client number application approved";
      case R -> "Client number application can’t go ahead";
      default -> businessName + " requires review";
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
      default -> revisionParameters(username, businessName, submissionId);
    };
  }

  private Map<String, Object> revisionParameters(
      String username,
      String businessName,
      Integer submissionId
  ) {
    return Map.of(
        "userName", username,
        "submission", submissionId,
        "business", Map.of(
            "name", businessName
        )
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
