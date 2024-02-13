package ca.bc.gov.app.service.legacy;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.dto.legacy.ForestClientContactDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.entity.SubmissionDetailEntity;
import ca.bc.gov.app.entity.SubmissionLocationEntity;
import ca.bc.gov.app.repository.SubmissionContactRepository;
import ca.bc.gov.app.repository.SubmissionDetailRepository;
import ca.bc.gov.app.repository.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.SubmissionLocationRepository;
import ca.bc.gov.app.repository.SubmissionRepository;
import java.time.Duration;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * This class is responsible for persisting the submission into the legacy database.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public abstract class LegacyAbstractPersistenceService {

  @Getter
  private final SubmissionDetailRepository submissionDetailRepository;
  private final SubmissionRepository submissionRepository;
  private final SubmissionLocationRepository locationRepository;
  @Getter
  private final SubmissionContactRepository contactRepository;
  private final SubmissionLocationContactRepository locationContactRepository;
  private final LegacyService legacyService;

  abstract Mono<MessagingWrapper<ForestClientDto>> generateForestClient(
      MessagingWrapper<String> message);

  abstract boolean filterByType(String clientTypeCode);

  /**
   * Loads the submission from the database and prepares the message for next step.
   */
  public Mono<MessagingWrapper<Integer>> loadSubmission(MessagingWrapper<Integer> message) {
    return submissionRepository
        .findById(message.payload())
        .doOnNext(submission -> log.info("Loaded submission for persistence on oracle {}",
            submission.getSubmissionId())
        )
        .map(submission ->
            new MessagingWrapper<>(
                message.payload(),
                message.parameters()
            )
                .withParameter(ApplicationConstant.SUBMISSION_ID, message.payload())
                .withParameter(ApplicationConstant.CREATED_BY, submission.getCreatedBy())
                .withParameter(ApplicationConstant.UPDATED_BY, submission.getUpdatedBy())
        );
  }

  /**
   * Checks if the client number exists for that submission and prepares the message for next step.
   * The next step will be identified and handled by individual channels
   */
  public Mono<MessagingWrapper<String>> checkClientData(MessagingWrapper<Integer> message) {
    return submissionDetailRepository
        .findBySubmissionId(message.payload())
        .doOnNext(
            submissionDetail ->
                log.info(
                    "Loaded submission detail for persistence on oracle {} {} {}",
                    message.payload(),
                    submissionDetail.getOrganizationName(),
                    submissionDetail.getRegistrationNumber()
                )
        )
        .flatMap(submissionDetail ->
            contactRepository
                .findFirstBySubmissionId(message.payload())
                .map(contact -> contact.getFirstName() + " " + contact.getLastName())
                .doOnNext(contact ->
                    log.info(
                        "Loaded submission contact for persistence on oracle {} {}",
                        message.payload(),
                        contact
                    )
                )
                .map(contact ->
                    new MessagingWrapper<>(
                        submissionDetail.getClientNumber(),
                        message.parameters()
                    )
                        .withParameter(ApplicationConstant.CLIENT_TYPE_CODE,
                            submissionDetail.getClientTypeCode())
                        .withParameter(ApplicationConstant.CLIENT_SUBMITTER_NAME,
                            contact
                        )
                )
        );
  }

  /**
   * Creates a client if does not exist on oracle and get back the client number.
   */
  public Mono<MessagingWrapper<Integer>> createForestClient(
      MessagingWrapper<ForestClientDto> message) {

    log.info("Creating Forest Client {} {}",
        message.parameters().get(ApplicationConstant.FOREST_CLIENT_NAME),
        message.payload().clientNumber()
    );

    return
        // Create the client
        legacyService
            .createClient(message.payload())
            .doOnNext(forestClientNumber ->
                log.info(
                    "Created forest client {} {}",
                    message.payload().clientNumber(),
                    forestClientNumber
                )
            )
            // Create the doing business as IF exists
            .flatMap(clientNumber -> createClientDoingBusinessAs(message, clientNumber))
            // Updates the submission detail with the client number
            .flatMap(clientNumber ->
                submissionDetailRepository
                    .findBySubmissionId(
                        (Integer) message.parameters().get(ApplicationConstant.SUBMISSION_ID)
                    )
                    .map(submissionDetail -> submissionDetail.withClientNumber(clientNumber))
                    .doOnNext(submissionDetail ->
                        log.info(
                            "Updating submission detail for persistence on oracle {} {} {}",
                            message.payload().clientNumber(),
                            submissionDetail.getOrganizationName(),
                            submissionDetail.getRegistrationNumber()
                        )
                    )
                    .flatMap(submissionDetailRepository::save)
                    .map(SubmissionDetailEntity::getClientNumber)
            )
            .doOnNext(forestClientNumber ->
                log.info(
                    "Saved forest client {} {}",
                    message.payload(),
                    forestClientNumber
                )
            )
            .map(forestClientNumber ->
                new MessagingWrapper<>(
                    (Integer) message.parameters().get(ApplicationConstant.SUBMISSION_ID),
                    message.parameters()
                )
                    .withParameter(ApplicationConstant.FOREST_CLIENT_NUMBER, forestClientNumber)
            );
  }

  /**
   * Creates a location if it does not exist on oracle. The creation happens by sending a request to
   * the legacy service.
   *
   * @param message A message containing the submission id
   * @return A flux of messages containing the submission id and the location id
   */
  public Flux<MessagingWrapper<Integer>> createLocations(MessagingWrapper<Integer> message) {

    Flux<SubmissionLocationEntity> data = locationRepository.findBySubmissionId(
            message.payload()
        )
        .doOnNext(submissionLocation ->
            log.info(
                "Loaded submission location [{}] {} for persistence on oracle for submission {}",
                submissionLocation.getSubmissionLocationId(),
                submissionLocation.getName(),
                message.payload()
            )
        );

    return data
        .index((index, detail) ->
            legacyService
                .createLocation(
                    detail,
                    getClientNumber(message),
                    index,
                    getUser(message, ApplicationConstant.CREATED_BY)
                )
                .delayElement(Duration.ofSeconds(1)) //This is to prevent same creation time
                .map(forestClient ->
                    new MessagingWrapper<>(
                        message.payload(),
                        message.parameters()
                    )
                        .withParameter(ApplicationConstant.LOCATION_ID,
                            detail.getSubmissionLocationId()
                        )
                        .withParameter(ApplicationConstant.LOCATION_CODE,
                            String.format("%02d", index))
                )
        )
        .flatMap(Function.identity());

  }

  /**
   * Creates a contact if does not exist on oracle. It first checks for an existing entry and if it
   * does not have, create it.
   */
  public Mono<MessagingWrapper<Integer>> createContact(MessagingWrapper<Integer> message) {

    // Load all contacts for this location
    return locationContactRepository
        .findBySubmissionLocationId(
            (Integer) message.parameters().get(ApplicationConstant.LOCATION_ID)
        )
        // Load the contact detail
        .flatMap(locationContact ->
            contactRepository
                .findById(locationContact.getSubmissionContactId())
        )
        // Log the contact detail
        .doOnNext(submissionContact ->
            log.info(
                "Loaded submission contact for persistence on oracle Submission: {} Contact {} {} Location Code {}",
                message.payload(),
                submissionContact.getFirstName(),
                submissionContact.getLastName(),
                message.parameters().get(ApplicationConstant.LOCATION_CODE)
            )
        )
        //Delay to avoid conflicts
        .delayElements(Duration.ofSeconds(1), Schedulers.newSingle("contact-creation"))
        // Convert it to a DTO
        .map(submissionContact ->
            new ForestClientContactDto(
                getClientNumber(message),
                message.parameters().get(ApplicationConstant.LOCATION_CODE).toString(),
                submissionContact.getContactTypeCode(),
                String.format("%s %s", submissionContact.getFirstName(),
                    submissionContact.getLastName()).toUpperCase(),
                RegExUtils.replaceAll(submissionContact.getBusinessPhoneNumber(), "\\D",
                    StringUtils.EMPTY),
                submissionContact.getEmailAddress(),
                ApplicationConstant.PROCESSOR_USER_NAME,
                ApplicationConstant.PROCESSOR_USER_NAME,
                ApplicationConstant.ORG_UNIT
            )
        )
        // Send it to the legacy service for processing
        .flatMap(legacyService::createContact)
        // Gets all back in sequence
        .collectList()
        // Move on to the next step
        .thenReturn(message);
  }

  protected ForestClientDto getBaseForestClient(String createdBy, String updatedBy) {
    return
        new ForestClientDto(
            null,
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            "ACT",
            StringUtils.EMPTY,
            null,
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            createdBy,
            updatedBy,
            ApplicationConstant.ORG_UNIT
        );
  }

  protected String getUser(MessagingWrapper<?> message, String headerName) {
    return message.parameters().get(headerName).toString();
  }

  private String getClientNumber(MessagingWrapper<?> message) {
    return message.parameters().get(ApplicationConstant.FOREST_CLIENT_NUMBER).toString();
  }

  private boolean isRegisteredSoleProprietorship(ForestClientDto forestClient) {
    return forestClient.clientTypeCode().equalsIgnoreCase("I") && StringUtils.equalsIgnoreCase(
        forestClient.clientIdTypeCode(), "BCRE");
  }

  private Mono<String> createClientDoingBusinessAs(MessagingWrapper<ForestClientDto> message,
      String clientNumber) {
    return Mono
        .just(clientNumber)
        .filter(
            forestClientNumber -> isRegisteredSoleProprietorship(message.payload())
        )
        .doOnNext(forestClientNumber ->
            log.info(
                "Creating doing business as for {} {}",
                forestClientNumber,
                message.parameters().get(ApplicationConstant.FOREST_CLIENT_NAME)
            )
        )
        .flatMap(forestClientNumber ->
            legacyService
                .createDoingBusinessAs(
                    forestClientNumber,
                    message.parameters().get(ApplicationConstant.FOREST_CLIENT_NAME).toString(),
                    getUser(message, ApplicationConstant.CREATED_BY),
                    getUser(message, ApplicationConstant.UPDATED_BY)
                )
        )
        .defaultIfEmpty(clientNumber)
        .doOnNext(forestClientNumber ->
            log.info(
                "Created doing business as for {} {}",
                forestClientNumber,
                message.parameters().get(ApplicationConstant.FOREST_CLIENT_NAME)
            )
        );
  }

}
