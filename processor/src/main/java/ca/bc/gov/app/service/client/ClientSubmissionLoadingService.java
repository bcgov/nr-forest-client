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
                    details.getRegistrationNumber(),
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

  public Mono<EmailRequestDto> buildMailMessage(MessagingWrapper<Integer> message) {
    if (message
          .parameters()
          .get(ApplicationConstant.SUBMISSION_STATUS) == null
        ) {
      return Mono.empty();
    }

    return submissionDetailRepository
            .findBySubmissionId(message.payload())
            .doOnNext(
                submission -> log.info("Loaded submission details for mail purpose {}", submission)
            )
            .flatMap(details ->
              contactRepository
                .findFirstBySubmissionId(message.payload())
                .flatMap(submissionContact -> Mono.just(isSubmissionStatusNew(message))
                .filter(Boolean::booleanValue)
                .flatMap(isAdmin -> getDistrictEmailsAndDescription(details.getDistrictCode())
                    .map(districtInfo -> 
                      new EmailRequestDto(
                          details.getRegistrationNumber(),
                          details.getOrganizationName(), 
                          submissionContact.getUserId(),
                          submissionContact.getFirstName(), 
                          districtInfo.getLeft(),
                          getTemplate(message), 
                          getSubject(message, details.getOrganizationName()),
                          getParameter(message, submissionContact.getFirstName(),
                          details.getOrganizationName(), 
                          districtInfo.getRight(),
                          Objects.toString(details.getClientNumber(), ""),
                          String.valueOf(message.parameters().get(ApplicationConstant.MATCHING_REASON)),
                          message.payload()
                      )
                    )
                )
            )
          )
      );
  }

  private Mono<Pair<String, String>> getDistrictEmailsAndDescription(String districtCode) {
    return forestClientApi
            .get()
            .uri("/districts/{districtCode}", districtCode)
            .exchangeToMono(clientResponse -> clientResponse.bodyToMono(DistrictDto.class))
            .doOnNext(district -> log.info("Loaded district details {} {}", 
                                            district.code(),
                                            district.description()))
            .map(district -> Pair.of(district.emails(), district.description()));
  }

  private boolean isSubmissionStatusNew(MessagingWrapper<Integer> message) {
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
      String districtName,
      String clientNumber,
      String reason,
      Integer submissionId
  ) {
    return switch ((SubmissionStatusEnum) message.parameters()
        .get(ApplicationConstant.SUBMISSION_STATUS)) {
      case A -> approvalParameters(username, businessName, clientNumber);
      case R -> rejectionParameters(username, businessName, clientNumber, reason);
      default -> revisionParameters(username, businessName, submissionId, districtName);
    };
  }

  private Map<String, Object> revisionParameters(
      String username,
      String businessName,
      Integer submissionId,
      String districtName
  ) {
    return Map.of(
        "userName", username,
        "submission", submissionId,
        "business", Map.of(
            "name", businessName,
            "districtName", districtName
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
