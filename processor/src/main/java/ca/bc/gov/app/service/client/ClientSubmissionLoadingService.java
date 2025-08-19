package ca.bc.gov.app.service.client;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.DistrictDto;
import ca.bc.gov.app.dto.EmailRequestDto;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.entity.SubmissionStatusEnum;
import ca.bc.gov.app.repository.SubmissionContactRepository;
import ca.bc.gov.app.repository.SubmissionDetailRepository;
import ca.bc.gov.app.repository.SubmissionRepository;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

  private static final String KEY_USER_NAME = "userName";
  private static final String KEY_SUBMISSION = "submission";
  private static final String KEY_BUSINESS = "business";
  private static final String KEY_NAME = "name";
  private static final String KEY_DISTRICT_NAME = "districtName";
  private static final String KEY_CLIENT_NUMBER = "clientNumber";
  private static final String KEY_DISTRICT_EMAIL = "districtEmail";
  private static final String KEY_REASON = "reason";
  
  private final SubmissionRepository submissionRepository;
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
            .doOnNext(submission -> log.info("Submission details loaded for id {}", submission.getSubmissionId()))
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
    if (message.parameters().get(ApplicationConstant.SUBMISSION_STATUS) == null) {
      return Mono.empty();
    }

    return submissionRepository
        .findBySubmissionId(message.payload()) // Fetch SubmissionEntity
        .doOnNext(submission ->
            log.info("Submission loaded for mail purpose {} - Notify client? {}", 
                submission.getSubmissionId(),
                submission.getNotifyClientInd()) // Use notifyClientInd from SubmissionEntity
        )
        .flatMap(submission ->
            submissionDetailRepository
                .findBySubmissionId(message.payload()) // Fetch SubmissionDetailEntity
                .doOnNext(details ->
                    log.info("Submission details loaded for mail purpose {} - District: {}", 
                        details.getSubmissionId(),
                        details.getDistrictCode()) // Log district information
                )
                .flatMap(details ->
                    contactRepository
                        .findFirstBySubmissionId(message.payload())
                        .doOnNext(submissionContact ->
                            log.info("Submission contact loaded for mail purpose {} [{} {}]",
                                submissionContact.getSubmissionId(),
                                submissionContact.getFirstName(),
                                submissionContact.getLastName())
                        )
                        .flatMap(submissionContact ->
                            getDistrictEmailsAndDescription(details.getDistrictCode())
                                .doOnNext(districtInfo ->
                                    log.info("District email and description loaded for mail purpose {} {} [{}]",
                                        message.payload(),
                                        districtInfo.getLeft(),
                                        districtInfo.getRight())
                                )
                                .switchIfEmpty(Mono.just(Pair.of(StringUtils.EMPTY, StringUtils.EMPTY))
                                    .doOnNext(districtInfo ->
                                        log.info("No district email and description found for mail purpose {} [{}]",
                                            message.payload(),
                                            details.getDistrictCode())
                                    )
                                )
                                .flatMap(districtInfo -> {
                                  // Exclude client email if notifyClientInd is "N" 
                                  // and there is an email
                                  String clientEmail = 
                                      submission.getNotifyClientInd().equalsIgnoreCase("N")
                                      && StringUtils.isNotEmpty(
                                          submissionContact.getEmailAddress()
                                        )
                                        ? null
                                        : submissionContact.getEmailAddress();

                                  String rawEmails = getEmails(
                                      message, 
                                      districtInfo.getLeft(), 
                                      clientEmail);

                                  // Avoid sending an email if no recipient emails exist
                                  if (StringUtils.isBlank(rawEmails)) {
                                    log.info("No recipients for email. Skipping email creation.");
                                    return Mono.empty();
                                  }

                                  return Mono.just(new EmailRequestDto(
                                      details.getRegistrationNumber(),
                                      details.getOrganizationName(),
                                      submissionContact.getUserId(),
                                      submissionContact.getFirstName(),
                                      rawEmails,
                                      getTemplate(message),
                                      getSubject(message, details.getOrganizationName()),
                                      getParameter(message,
                                          submissionContact.getFirstName() + " " + submissionContact.getLastName(),
                                          details.getOrganizationName(),
                                          districtInfo.getRight(),
                                          districtInfo.getLeft(),
                                          Objects.toString(details.getClientNumber(), ""),
                                          String.valueOf(message.parameters().get(ApplicationConstant.MATCHING_REASON)),
                                          message.payload())
                                  ));
                                })
                        )
                )
        );
  }

  private Mono<Pair<String, String>> getDistrictEmailsAndDescription(String districtCode) {
    return
        StringUtils.isBlank(districtCode) ?
            Mono.just(Pair.of(StringUtils.EMPTY, StringUtils.EMPTY))
            :
                forestClientApi
                    .get()
                    .uri("/codes/districts/{districtCode}", districtCode)
                    .exchangeToMono(clientResponse -> clientResponse.bodyToMono(DistrictDto.class))
                    .doOnNext(district -> log.info("Loaded district details {} {}",
                        district.code(),
                        district.description()))
                    .map(district -> Pair.of(district.emails(), district.description()))
                    .onErrorReturn(Pair.of(StringUtils.EMPTY, StringUtils.EMPTY))
        ;
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

  private String getEmails(
      MessagingWrapper<Integer> message,
      String districtEmails,
      String userEmail
  ) {
    Stream<String> emails = switch ((SubmissionStatusEnum) message.parameters()
        .get(ApplicationConstant.SUBMISSION_STATUS)) {
      case A, R -> Stream.of(districtEmails, userEmail);
      default -> Stream.of(districtEmails);
    };

    return emails
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.joining(","));
  }

  private Map<String, Object> getParameter(
      MessagingWrapper<Integer> message,
      String username,
      String businessName,
      String districtName,
      String districtEmail,
      String clientNumber,
      String reason,
      Integer submissionId
  ) {
    return switch ((SubmissionStatusEnum) message.parameters()
        .get(ApplicationConstant.SUBMISSION_STATUS)) {
      case A -> approvalParameters(username, businessName, clientNumber, districtName, districtEmail);
      case R -> rejectionParameters(username, businessName, clientNumber, reason, districtName, districtEmail);
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
        KEY_USER_NAME, username,
        KEY_SUBMISSION, submissionId,
        KEY_BUSINESS, Map.of(
            KEY_NAME, businessName,
            KEY_DISTRICT_NAME, districtName
        )
    );
  }

  private Map<String, Object> approvalParameters(
      String username,
      String businessName,
      String clientNumber,
      String districtName,
      String districtEmail
  ) {
    return Map.of(
        KEY_USER_NAME, username,
        KEY_BUSINESS, Map.of(
            KEY_NAME, businessName,
            KEY_CLIENT_NUMBER, clientNumber,
            KEY_DISTRICT_NAME, districtName,
            KEY_DISTRICT_EMAIL, districtEmail
        )
    );
  }

  private Map<String, Object> rejectionParameters(
      String username,
      String businessName,
      String clientNumber,
      String reason,
      String districtName,
      String districtEmail
  ) {
    return Map.of(
        KEY_USER_NAME, username,
        KEY_REASON, reason,
        KEY_BUSINESS, Map.of(
            KEY_NAME, businessName,
            KEY_CLIENT_NUMBER, clientNumber,
            KEY_DISTRICT_NAME, districtName,
            KEY_DISTRICT_EMAIL, districtEmail
        )
    );
  }

}
