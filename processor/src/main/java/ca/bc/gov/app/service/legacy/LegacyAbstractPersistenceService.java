package ca.bc.gov.app.service.legacy;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.legacy.ForestClientContactDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationEntity;
import ca.bc.gov.app.entity.client.SubmissionTypeCodeEnum;
import ca.bc.gov.app.repository.client.SubmissionContactRepository;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import ca.bc.gov.app.util.ProcessorUtil;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

  abstract Mono<Message<ForestClientDto>> generateForestClient(Message<String> message);

  abstract boolean filterByType(String clientTypeCode);

  abstract String getNextChannel();


  /**
   * Loads the submission from the database and prepares the message for next step.
   */
  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_LEGACY_CHANNEL,
      outputChannel = ApplicationConstant.SUBMISSION_LEGACY_CLIENT_CHANNEL,
      async = "true"
  )
  public Mono<Message<Integer>> loadSubmission(Message<Integer> message) {
    return submissionRepository
        .findById(message.getPayload())
        .doOnNext(submission -> log.info("Loaded submission for persistence on oracle {}",
            submission.getSubmissionId())
        )
        .map(submission -> MessageBuilder
            .fromMessage(message)
            .setHeader(ApplicationConstant.SUBMISSION_ID, message.getPayload())
            .setHeader(ApplicationConstant.CREATED_BY, submission.getCreatedBy())
            .setHeader(ApplicationConstant.UPDATED_BY, submission.getUpdatedBy())
            .build()
        );
  }


  /**
   * Checks if the client number exists for that submission and prepares the message for next step.
   * The next step will be identified and handled by individual channels
   */
  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_LEGACY_CLIENT_CHANNEL,
      outputChannel = ApplicationConstant.FORWARD_CHANNEL, //Dummy channel name
      async = "true"
  )
  public Mono<Message<?>> checkClientData(Message<Integer> message) {
    return submissionDetailRepository
        .findBySubmissionId(message.getPayload())
        .doOnNext(
            submissionDetail ->
                log.info(
                    "Loaded submission detail for persistence on oracle {} {} {}",
                    message.getPayload(),
                    submissionDetail.getOrganizationName(),
                    submissionDetail.getIncorporationNumber()
                )
        )
        .filter(submissionDetail -> filterByType(submissionDetail.getClientTypeCode()))
        .flatMap(submissionDetail ->
            contactRepository
                .findFirstBySubmissionId(message.getPayload())
                .map(contact ->
                    MessageBuilder
                        .withPayload(message.getPayload())
                        .copyHeaders(message.getHeaders())
                        .setHeader(ApplicationConstant.SUBMISSION_ID, message.getPayload())
                        .setHeader(ApplicationConstant.CLIENT_TYPE_CODE,
                            submissionDetail.getClientTypeCode())
                        .setHeader(ApplicationConstant.CLIENT_SUBMITTER_NAME,
                            contact.getFirstName() + " " + contact.getLastName())
                        .setReplyChannelName(getNextChannel())
                        .setHeader("output-channel", getNextChannel())
                        .setHeader(MessageHeaders.REPLY_CHANNEL, getNextChannel())
                        .build()
                )
        );
  }

  /**
   * Creates a client if does not exist on oracle and get back the client number.
   */
  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_LEGACY_CLIENT_PERSIST_CHANNEL,
      outputChannel = ApplicationConstant.SUBMISSION_LEGACY_LOCATION_CHANNEL,
      async = "true"
  )
  public Mono<Message<Integer>> createForestClient(Message<ForestClientDto> message) {

    if (!filterByType(
        message.getHeaders().get(ApplicationConstant.CLIENT_TYPE_CODE, String.class))) {
      return Mono.empty();
    }

    log.info("Creating Forest Client {} {}",
        message.getHeaders().get(ApplicationConstant.FOREST_CLIENT_NAME),
        message.getPayload().clientNumber()
    );

    return
        // Create the client
        legacyService
            .createClient(message.getPayload())
            // Create the doing business as IF exists
            .flatMap(clientNumber -> createClientDoingBusinessAs(message, clientNumber))
            // Updates the submission detail with the client number
            .flatMap(clientNumber ->
                submissionDetailRepository
                    .findBySubmissionId(
                        message.getHeaders().get(ApplicationConstant.SUBMISSION_ID, Integer.class)
                    )
                    .map(submissionDetail -> submissionDetail.withClientNumber(clientNumber))
                    .flatMap(submissionDetailRepository::save)
                    .map(SubmissionDetailEntity::getClientNumber)
            )
            .doOnNext(forestClientNumber ->
                log.info(
                    "Saved forest client {} {}",
                    message.getPayload(),
                    forestClientNumber
                )
            )
            .map(forestClientNumber ->
                MessageBuilder
                    .withPayload(
                        message.getHeaders().get(ApplicationConstant.SUBMISSION_ID, Integer.class))
                    .copyHeaders(message.getHeaders())
                    .setHeader(ApplicationConstant.FOREST_CLIENT_NUMBER, forestClientNumber)
                    .build()
            );
  }

  /**
   * Creates a location if it does not exist on oracle. The creation happens by sending a request to
   * the legacy service.
   *
   * @param message A message containing the submission id
   * @return A flux of messages containing the submission id and the location id
   */
  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_LEGACY_LOCATION_CHANNEL,
      outputChannel = ApplicationConstant.SUBMISSION_LEGACY_CONTACT_CHANNEL,
      async = "true"
  )
  public Flux<Message<Integer>> createLocations(Message<Integer> message) {

    if (!filterByType(
        message.getHeaders().get(ApplicationConstant.CLIENT_TYPE_CODE, String.class))) {
      return Flux.empty();
    }

    Flux<SubmissionLocationEntity> data = locationRepository.findBySubmissionId(
            message.getPayload()
        )
        .doOnNext(submissionLocation ->
            log.info(
                "Loaded submission location for persistence on oracle {} {} {}",
                message.getPayload(),
                submissionLocation.getName(),
                submissionLocation.getSubmissionLocationId()
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
                .flatMap(forestClient ->
                    data
                        .count()
                        .doOnNext(count ->
                            log.info(
                                "Proceeding with location {}/{} of submission id {}",
                                index,
                                count,
                                message.getPayload()
                            )
                        )
                        .map(count ->
                            MessageBuilder
                                .fromMessage(message)
                                .copyHeaders(message.getHeaders())
                                .setHeader(ApplicationConstant.LOCATION_CODE,
                                    String.format("%02d", index))
                                .setHeader(ApplicationConstant.LOCATION_ID,
                                    detail.getSubmissionLocationId())
                                .setHeader(ApplicationConstant.TOTAL, count)
                                .setHeader(ApplicationConstant.INDEX, index)
                                .build()
                        )
                )
        )
        .flatMap(Function.identity());

  }

  /**
   * Creates a contact if does not exist on oracle. It first checks for an existing entry and if it
   * does not have, create it.
   */
  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_LEGACY_CONTACT_CHANNEL,
      outputChannel = ApplicationConstant.SUBMISSION_LEGACY_NOTIFY_CHANNEL,
      async = "true"
  )
  public Mono<Message<Integer>> createContact(Message<Integer> message) {

    if (!filterByType(
        message.getHeaders().get(ApplicationConstant.CLIENT_TYPE_CODE, String.class))) {
      return Mono.empty();
    }
    // Load all contacts for this location
    return locationContactRepository
        .findBySubmissionLocationId(
            message.getHeaders().get(ApplicationConstant.LOCATION_ID, Integer.class)
        )
        // Load the contact detail
        .flatMap(locationContact ->
            contactRepository
                .findById(locationContact.getSubmissionContactId())
        )
        // Log the contact detail
        .doOnNext(submissionContact ->
            log.info(
                "Loaded submission contact for persistence on oracle {} {} {} {}",
                message.getPayload(),
                submissionContact.getFirstName(),
                submissionContact.getLastName(),
                message.getHeaders().get(ApplicationConstant.LOCATION_CODE, String.class)
            )
        )
        // Convert it to a DTO
        .map(submissionContact ->
            new ForestClientContactDto(
                getClientNumber(message),
                message.getHeaders().get(ApplicationConstant.LOCATION_CODE, String.class),
                submissionContact.getContactTypeCode(),
                String.format("%s %s", submissionContact.getFirstName(),
                    submissionContact.getLastName()).toUpperCase(),
                RegExUtils.replaceAll(submissionContact.getBusinessPhoneNumber(), "\\D",
                    StringUtils.EMPTY),
                submissionContact.getEmailAddress(),
                getUser(message, ApplicationConstant.CREATED_BY),
                getUser(message, ApplicationConstant.UPDATED_BY),
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


  /**
   * Sends a notification to the user that the submission has been processed
   */
  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_LEGACY_NOTIFY_CHANNEL,
      outputChannel = ApplicationConstant.SUBMISSION_MAIL_BUILD_CHANNEL,
      async = "true"
  )
  public Mono<Message<Integer>> sendNotification(Message<Integer> message) {

    if (!filterByType(
        message.getHeaders().get(ApplicationConstant.CLIENT_TYPE_CODE, String.class))) {
      return Mono.empty();
    }

    Long total = message.getHeaders().get(ApplicationConstant.TOTAL, Long.class);
    Long index = message.getHeaders().get(ApplicationConstant.INDEX, Long.class);

    if ((total == null || index == null) || ((total - 1) > index)) {
      log.info("Skipping notification for submission {} until last location is processed",
          message.getHeaders().get(ApplicationConstant.SUBMISSION_ID, Integer.class)
      );
      return Mono.empty();
    }

    log.info("Sending notification for submission {}",
        message.getHeaders().get(ApplicationConstant.SUBMISSION_ID, Integer.class)
    );
    return Mono.just(
        MessageBuilder
            .fromMessage(message)
            .setHeader(ApplicationConstant.SUBMISSION_TYPE, SubmissionTypeCodeEnum.RAC)
            .build()
    );
  }

  protected ForestClientDto getBaseForestClient(String createdBy, String updatedBy) {
    return
        new ForestClientDto(
            "00",
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

  protected String getUser(Message<?> message, String headerName) {
    return ProcessorUtil
        .readHeader(
            message,
            headerName,
            String.class
        )
        .orElse(ApplicationConstant.PROCESSOR_USER_NAME);
  }

  private String getClientNumber(Message<?> message) {
    return ProcessorUtil.readHeader(message, ApplicationConstant.FOREST_CLIENT_NUMBER, String.class)
        .orElse(StringUtils.EMPTY);
  }

  private boolean isRegisteredSoleProprietorship(ForestClientDto forestClient) {
    return forestClient.clientTypeCode().equalsIgnoreCase("I") && StringUtils.equalsIgnoreCase(
        forestClient.clientIdTypeCode(), "BCRE");
  }

  private Mono<String> createClientDoingBusinessAs(Message<ForestClientDto> message,
      String clientNumber) {
    return Mono
        .just(clientNumber)
        .filter(
            forestClientNumber -> isRegisteredSoleProprietorship(message.getPayload())
        )
        .flatMap(forestClientNumber ->
            legacyService
                .createDoingBusinessAs(
                    forestClientNumber,
                    message.getHeaders().get(ApplicationConstant.FOREST_CLIENT_NAME,
                        String.class),
                    getUser(message, ApplicationConstant.CREATED_BY),
                    getUser(message, ApplicationConstant.UPDATED_BY)
                )
        )
        .defaultIfEmpty(clientNumber);
  }

}
