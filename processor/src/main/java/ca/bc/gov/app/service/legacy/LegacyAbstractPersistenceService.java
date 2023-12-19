package ca.bc.gov.app.service.legacy;

import static java.util.function.Predicate.not;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.entity.client.SubmissionContactEntity;
import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationContactEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationEntity;
import ca.bc.gov.app.entity.client.SubmissionTypeCodeEnum;
import ca.bc.gov.app.entity.legacy.ClientDoingBusinessAsEntity;
import ca.bc.gov.app.entity.legacy.ForestClientContactEntity;
import ca.bc.gov.app.repository.client.CountryCodeRepository;
import ca.bc.gov.app.repository.client.SubmissionContactRepository;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import ca.bc.gov.app.repository.legacy.ClientDoingBusinessAsRepository;
import ca.bc.gov.app.util.ProcessorUtil;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.IntFunction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
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
  private final R2dbcEntityOperations legacyR2dbcEntityTemplate;
  private final ClientDoingBusinessAsRepository doingBusinessAsRepository;
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

    AtomicBoolean existingClient = new AtomicBoolean(false);
    AtomicReference<String> clientTypeCode = new AtomicReference<>(StringUtils.EMPTY);

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
        .doOnNext(submissionDetail ->
            clientTypeCode.set(submissionDetail.getClientTypeCode()))
        .flatMap(submissionDetail ->
            Mono.justOrEmpty(
                    Optional
                        .ofNullable(submissionDetail.getClientNumber())
                        .filter(StringUtils::isNotBlank)
                )
                .doOnNext(clientNumber -> log.info(
                    "Client number {} exists for submission {}",
                    clientNumber,
                    message.getPayload()
                ))
                .doOnNext(clientNumber -> existingClient.set(true))
                .switchIfEmpty(getNextClientNumber())
        )
        .filter(data -> filterByType(clientTypeCode.get()))
        .doOnNext(clientNumber ->
            log.info(
                "Client number {}{} for submission {}",
                clientNumber,
                existingClient.get() ? " exists" : " is new",
                message.getPayload()
            )
        )
        .flatMap(clientNumber ->

            contactRepository
                .findFirstBySubmissionId(message.getPayload())
                .map(contact ->
                    MessageBuilder
                        .withPayload(existingClient.get() ? message.getPayload() : clientNumber)
                        .copyHeaders(message.getHeaders())
                        .setHeader(ApplicationConstant.SUBMISSION_ID, message.getPayload())
                        .setHeader(ApplicationConstant.CLIENT_EXISTS, existingClient.get())
                        .setHeader(ApplicationConstant.CLIENT_TYPE_CODE, clientTypeCode.get())
                        .setHeader(ApplicationConstant.FOREST_CLIENT_NUMBER, clientNumber)
                        .setHeader(ApplicationConstant.CLIENT_SUBMITTER_NAME,
                            contact.getFirstName() + " " + contact.getLastName())
                        .setReplyChannelName(
                            existingClient.get()
                                ? ApplicationConstant.SUBMISSION_LEGACY_LOCATION_CHANNEL
                                : getNextChannel())
                        .setHeader("output-channel",
                            existingClient.get()
                                ? ApplicationConstant.SUBMISSION_LEGACY_LOCATION_CHANNEL
                                : getNextChannel())
                        .setHeader(MessageHeaders.REPLY_CHANNEL,
                            existingClient.get()
                                ? ApplicationConstant.SUBMISSION_LEGACY_LOCATION_CHANNEL
                                : getNextChannel())
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
        message.getPayload().getClientNumber()
    );
    return
        legacyR2dbcEntityTemplate
            .insert(ForestClientEntity.class)
            .using(message.getPayload())
            .flatMap(forestClient ->
                Mono.just(isRegisteredSoleProprietorship(forestClient))
                    .filter(Boolean::booleanValue)
                    .flatMap(isRSP -> createClientDoingBusinessAs(message, forestClient))
                    .thenReturn(forestClient.getClientNumber())
                    .defaultIfEmpty(forestClient.getClientNumber())
            )
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
   * Creates a location if it does not exist on oracle.
   * The creation happens by sending a request to the legacy service.
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

    // Load the contact in case it exists
    IntFunction<Mono<ForestClientContactEntity>> forestContact =
        contactId ->
            contactRepository
                .findById(contactId)
                .flatMapMany(submissionContact ->
                    legacyR2dbcEntityTemplate
                        .select(
                            Query
                                .query(
                                    Criteria
                                        .where(ApplicationConstant.CLIENT_NUMBER)
                                        .is(getClientNumber(message))
                                        .and("CLIENT_LOCN_CODE").is(
                                            Objects.requireNonNull(message.getHeaders()
                                                .get(ApplicationConstant.LOCATION_CODE, String.class)
                                            )
                                        )
                                        .and("CONTACT_NAME").is(
                                            String.format("%s %s",
                                                submissionContact.getFirstName().toUpperCase(),
                                                submissionContact.getLastName().toUpperCase())
                                        )
                                ),
                            ForestClientContactEntity.class
                        )
                )
                .next()
                .doOnNext(forestClientContact ->
                    log.info(
                        "Forest client contact {} {} already exists for submission {}",
                        forestClientContact.getClientContactId(),
                        forestClientContact.getContactName(),
                        message.getPayload()
                    )
                );

    // Load the contact and converts it into a forest client contact entity
    IntFunction<Mono<ForestClientContactEntity>> toContact = contactId ->
        contactRepository
            .findById(contactId)
            .doOnNext(submissionContact ->
                log.info(
                    "Loaded submission contact for persistence on oracle {} {} {} {}",
                    message.getPayload(),
                    submissionContact.getFirstName(),
                    submissionContact.getLastName(),
                    message.getHeaders().get(ApplicationConstant.LOCATION_CODE, String.class)
                )
            )
            .map(this::toForestClientContactEntity)
            .doOnNext(forestClient -> forestClient.setCreatedBy(getUser(message,
                ApplicationConstant.CREATED_BY)))
            .doOnNext(forestClient -> forestClient.setUpdatedBy(getUser(message,
                ApplicationConstant.UPDATED_BY)))
            .doOnNext(forestClient -> forestClient.setClientNumber(getClientNumber(message)))
            .doOnNext(forestClientContact -> forestClientContact.setClientLocnCode(
                    message.getHeaders().get(ApplicationConstant.LOCATION_CODE, String.class)
                )
            );

    // Convert the contact into a forest client contact entity and save it
    Function<SubmissionLocationContactEntity, Mono<ForestClientContactEntity>> createContact =
        locationContact ->
            toContact
                .apply(locationContact.getSubmissionContactId())
                .flatMap(forestClientContact ->
                    getNextContactId()
                        .doOnNext(forestClientContact::setClientContactId)
                        .thenReturn(forestClientContact)
                )
                .flatMap(contact ->
                    legacyR2dbcEntityTemplate
                        .insert(ForestClientContactEntity.class)
                        .using(contact)
                )
                .doOnNext(forestClientContact ->
                    log.info(
                        "Saved forest client contact {} {} {}",
                        message.getPayload(),
                        getClientNumber(message),
                        forestClientContact.getContactName()
                    )
                );

    return locationContactRepository
        .findBySubmissionLocationId(
            message.getHeaders().get(ApplicationConstant.LOCATION_ID, Integer.class)
        )
        .flatMap(locationContactEntity ->
            forestContact
                .apply(locationContactEntity.getSubmissionContactId())
                .switchIfEmpty(createContact.apply(locationContactEntity))
        )
        .collectList()
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

  private Mono<String> getNextClientNumber() {
    return
        legacyR2dbcEntityTemplate
            .getDatabaseClient()
            .sql("""
                UPDATE
                max_client_nmbr
                SET
                client_number =  (SELECT LPAD(TO_NUMBER(NVL(max(CLIENT_NUMBER),'0'))+1,8,'0') FROM FOREST_CLIENT)"""
            )
            .fetch()
            .rowsUpdated()
            .then(
                legacyR2dbcEntityTemplate
                    .getDatabaseClient()
                    .sql("SELECT client_number FROM max_client_nmbr")
                    .map((row, rowMetadata) -> row.get("client_number", String.class))
                    .first()
            );
  }

  private Mono<Integer> getNextDoingBusinessAs() {
    return
        legacyR2dbcEntityTemplate
            .getDatabaseClient()
            .sql("select THE.client_dba_seq.NEXTVAL from dual")
            .fetch()
            .first()
            .map(row -> row.get("NEXTVAL"))
            .map(String::valueOf)
            .map(Integer::parseInt);
  }

  private Mono<String> getNextContactId() {
    return legacyR2dbcEntityTemplate
        .getDatabaseClient()
        .sql("SELECT THE.client_contact_seq.NEXTVAL FROM dual")
        .fetch()
        .first()
        .map(row -> row.get("NEXTVAL"))
        .map(String::valueOf);
  }

  private Mono<ClientDoingBusinessAsEntity> createClientDoingBusinessAs(
      Message<ForestClientDto> message, ForestClientDto forestClient) {
    return doingBusinessAsRepository.existsByClientNumber(forestClient.clientNumber())
        .filter(not(Boolean::booleanValue)).flatMap(doesNotExist -> getNextDoingBusinessAs()).map(
            nextId -> ClientDoingBusinessAsEntity.builder().id(nextId)
                .clientNumber(forestClient.clientNumber()).doingBusinessAsName(
                    message.getHeaders().get(ApplicationConstant.FOREST_CLIENT_NAME, String.class))
                .revision(1L).createdBy(ApplicationConstant.PROCESSOR_USER_NAME)
                .createdAt(LocalDateTime.now()).updatedBy(ApplicationConstant.PROCESSOR_USER_NAME)
                .updatedAt(LocalDateTime.now()).addOrgUnit(ApplicationConstant.ORG_UNIT)
                .updateOrgUnit(ApplicationConstant.ORG_UNIT).build()).flatMap(
            doingBusinessAs -> legacyR2dbcEntityTemplate.insert(ClientDoingBusinessAsEntity.class)
                .using(doingBusinessAs));
  }

  private boolean isRegisteredSoleProprietorship(ForestClientDto forestClient) {
    return forestClient.clientTypeCode().equalsIgnoreCase("I") && StringUtils.equalsIgnoreCase(
        forestClient.clientIdTypeCode(), "BCRE");
  }


  private String getClientNumber(Message<?> message) {
    return ProcessorUtil.readHeader(message, ApplicationConstant.FOREST_CLIENT_NUMBER, String.class)
        .orElse(StringUtils.EMPTY);
  }

  private ForestClientContactEntity toForestClientContactEntity(
      SubmissionContactEntity submissionContact
  ) {
    return ForestClientContactEntity
        .builder()
        .contactCode(submissionContact.getContactTypeCode())
        .contactName(String.format("%s %s", submissionContact.getFirstName(),
            submissionContact.getLastName()).toUpperCase())
        .businessPhone(
            RegExUtils.replaceAll(submissionContact.getBusinessPhoneNumber(), "\\D",
                StringUtils.EMPTY)
        )
        .emailAddress(submissionContact.getEmailAddress())
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .revision(1L)
        .createdBy(ApplicationConstant.PROCESSOR_USER_NAME)
        .updatedBy(ApplicationConstant.PROCESSOR_USER_NAME)
        .addOrgUnit(ApplicationConstant.ORG_UNIT)
        .updateOrgUnit(ApplicationConstant.ORG_UNIT)
        .build();
  }

}
