package ca.bc.gov.app.service.legacy;

import static java.util.function.Predicate.not;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.entity.client.SubmissionContactEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationContactEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationEntity;
import ca.bc.gov.app.entity.client.SubmissionTypeCodeEnum;
import ca.bc.gov.app.entity.legacy.ClientDoingBusinessAsEntity;
import ca.bc.gov.app.entity.legacy.ForestClientContactEntity;
import ca.bc.gov.app.entity.legacy.ForestClientEntity;
import ca.bc.gov.app.entity.legacy.ForestClientLocationEntity;
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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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
  private final CountryCodeRepository countryCodeRepository;
  private final ClientDoingBusinessAsRepository doingBusinessAsRepository;

  private final Map<String, String> countryList = new HashMap<>();


  abstract Mono<Message<ForestClientEntity>> generateForestClient(Message<String> message);
  abstract boolean filterByType(String clientTypeCode);

  /**
   * Loads the country list from the database.
   */
  @PostConstruct
  public void setUp() {
    countryCodeRepository
        .findAll()
        .doOnNext(countryCode ->
            countryList.put(
                countryCode.getCountryCode(),
                countryCode.getDescription()
            )
        )
        .collectList()
        .subscribe();
  }

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
    AtomicReference<String> sequenceChannel = new AtomicReference<>(
        ApplicationConstant.SUBMISSION_LEGACY_LOCATION_CHANNEL);

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
        .doOnNext(submissionDetail -> {
          // Future Improvement: Use the enum when we have more client types being processed
          if (submissionDetail.getClientTypeCode().equalsIgnoreCase("I")) {
            sequenceChannel.set(ApplicationConstant.SUBMISSION_LEGACY_INDIVIDUAL_CHANNEL);
          } else if (submissionDetail.getClientTypeCode().equalsIgnoreCase("RSP")) {
            sequenceChannel.set(ApplicationConstant.SUBMISSION_LEGACY_RSP_CHANNEL);
          } else if (submissionDetail.getClientTypeCode().equalsIgnoreCase("USP")) {
            sequenceChannel.set(ApplicationConstant.SUBMISSION_LEGACY_USP_CHANNEL);
          } else {
            sequenceChannel.set(ApplicationConstant.SUBMISSION_LEGACY_OTHER_CHANNEL);
          }
        })
        .flatMap(submissionDetail ->
            Mono.justOrEmpty(
                    Optional
                        .ofNullable(submissionDetail.getClientNumber())
                        .filter(StringUtils::isNotBlank)
                )
                .doOnNext(clientNumber -> {
                  existingClient.set(true);
                  sequenceChannel.set(ApplicationConstant.SUBMISSION_LEGACY_LOCATION_CHANNEL);
                })
                .switchIfEmpty(getNextClientNumber())
        )
        .filter(data -> filterByType(sequenceChannel.get()))
        .doOnNext(clientNumber ->
            log.info(
                "Client number {}{} for submission {}",
                clientNumber,
                existingClient.get() ? " exists" : " is new",
                message.getPayload()
            )
        )
        .map(clientNumber ->
            MessageBuilder
                .withPayload(existingClient.get() ? message.getPayload() : clientNumber)
                .copyHeaders(message.getHeaders())
                .setHeader(ApplicationConstant.SUBMISSION_ID, message.getPayload())
                .setHeader(ApplicationConstant.CLIENT_EXISTS, existingClient.get())

                .setReplyChannelName(sequenceChannel.get())
                .setHeader("output-channel", sequenceChannel.get())
                .setHeader(MessageHeaders.REPLY_CHANNEL, sequenceChannel.get())
                .setHeader(ApplicationConstant.CLIENT_TYPE_CODE, sequenceChannel.get())

                .setHeader(ApplicationConstant.FOREST_CLIENT_NUMBER, clientNumber)

                .build()
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
  public Mono<Message<Integer>> createForestClient(Message<ForestClientEntity> message) {

    if(!filterByType(message.getHeaders().get(ApplicationConstant.CLIENT_TYPE_CODE, String.class)))
      return Mono.empty();


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
                    .map(submissionDetail -> clientNumber)
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
   * Creates a location if does not exist on oracle.
   */
  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_LEGACY_LOCATION_CHANNEL,
      outputChannel = ApplicationConstant.SUBMISSION_LEGACY_CONTACT_CHANNEL,
      async = "true"
  )
  public Flux<Message<Integer>> createLocations(Message<Integer> message) {

    if(!filterByType(message.getHeaders().get(ApplicationConstant.CLIENT_TYPE_CODE, String.class)))
      return Flux.empty();

    Flux<SubmissionLocationEntity> data = locationRepository.findBySubmissionId(
        message.getPayload()
    );

    BiFunction<String, String, Mono<ForestClientLocationEntity>> locateClientLocation = (clientNumber, locationCode) ->
        legacyR2dbcEntityTemplate
            .selectOne(
                Query.query(
                    Criteria
                        .where("CLIENT_LOCN_CODE").is(locationCode)
                        .and(ApplicationConstant.CLIENT_NUMBER).is(clientNumber)
                ),
                ForestClientLocationEntity.class
            )
            .doOnNext(forestClientLocation ->
                log.info(
                    "Forest client location {} {} already exists for submission {}",
                    forestClientLocation.getClientLocnCode(),
                    forestClientLocation.getClientLocnName(),
                    message.getPayload()
                )
            );

    BiFunction<Long, SubmissionLocationEntity, Mono<ForestClientLocationEntity>> createClientLocation = (index, detail) ->
        toForestClientLocationEntity(index, detail)
            .doOnNext(submissionLocation ->
                log.info(
                    "Loaded submission location for persistence on oracle {} {} {}",
                    message.getPayload(),
                    submissionLocation.getClientLocnCode(),
                    submissionLocation.getClientLocnName()
                )
            )
            .doOnNext(forestClient -> forestClient.setCreatedBy(getUser(message,
                ApplicationConstant.CREATED_BY)))
            .doOnNext(forestClient -> forestClient.setUpdatedBy(getUser(message,
                ApplicationConstant.UPDATED_BY)))
            .doOnNext(forestClient -> forestClient.setClientNumber(getClientNumber(message)))
            .doOnNext(forestClient ->
                log.info(
                    "Saving forest client location {} {} {}",
                    message.getPayload(),
                    forestClient.getClientLocnName(),
                    forestClient
                )
            )
            .flatMap(forestClientLocation ->
                legacyR2dbcEntityTemplate
                    .insert(ForestClientLocationEntity.class)
                    .using(forestClientLocation)
            );

    return data
        .index((index, detail) ->
            locateClientLocation
                .apply(getClientNumber(message), String.format("%02d", index))
                .switchIfEmpty(createClientLocation.apply(index, detail))
                .flatMap(forestClient ->
                    data
                        .count()
                        .map(count ->
                            MessageBuilder
                                .fromMessage(message)
                                .copyHeaders(message.getHeaders())
                                .setHeader(ApplicationConstant.LOCATION_CODE,
                                    forestClient.getClientLocnCode())
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
   * Creates a contact if does not exist on oracle.
   * It first checks for an existing entry and if it does not have, create it.
   */
  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_LEGACY_CONTACT_CHANNEL,
      outputChannel = ApplicationConstant.SUBMISSION_LEGACY_AGGREGATE_CHANNEL,
      async = "true"
  )
  public Mono<Message<Integer>> createContact(Message<Integer> message) {

    if(!filterByType(message.getHeaders().get(ApplicationConstant.CLIENT_TYPE_CODE, String.class)))
      return Mono.empty();

    // Load the contact in case it exists
    IntFunction<Mono<ForestClientContactEntity>> forestContact =
        contactId ->
            contactRepository
                .findById(contactId)
                .flatMap(submissionContact ->
                    legacyR2dbcEntityTemplate
                        .selectOne(
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
                                            String.format("%s %s", submissionContact.getFirstName(),
                                                submissionContact.getLastName())
                                        )
                                ),
                            ForestClientContactEntity.class
                        )
                )
                .doOnNext(forestClientContact ->
                    log.info(
                        "Forest client contact {} {} already exists for submission {}",
                        forestClientContact.getClientContactId(),
                        forestClientContact.getContactName(),
                        message.getPayload()
                    )
                );

    // Load the next contact id
    IntFunction<Mono<String>> nextContactId = increment -> legacyR2dbcEntityTemplate
        .selectOne(
            Query
                .empty()
                .sort(Sort.by(Direction.DESC, "CLIENT_CONTACT_ID"))
                .limit(1),
            ForestClientContactEntity.class
        )
        .map(ForestClientContactEntity::getClientContactId)
        .map(lastForestClientContactId -> String.valueOf(
            Integer.parseInt(lastForestClientContactId) + increment));

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
    BiFunction<SubmissionLocationContactEntity,Integer, Mono<ForestClientContactEntity>> createContact =
        (locationContact,increment) ->
        toContact
            .apply(locationContact.getSubmissionContactId())
            .flatMap(forestClientContact ->
                nextContactId
                    .apply(increment)
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
        .index()
        .flatMap(locationContactTuple ->
            forestContact
                .apply(locationContactTuple.getT2().getSubmissionContactId())
                .switchIfEmpty(
                    createContact
                        .apply(
                            locationContactTuple.getT2(),
                            locationContactTuple.getT1().intValue()+1
                        )
                )
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
    if(!filterByType(message.getHeaders().get(ApplicationConstant.CLIENT_TYPE_CODE, String.class)))
      return Mono.empty();
    return Mono.just(
        MessageBuilder
            .fromMessage(message)
            .setHeader(ApplicationConstant.SUBMISSION_TYPE, SubmissionTypeCodeEnum.RAC)
            .build()
    );
  }

  protected ForestClientEntity getBaseForestClient(
      String createdBy,
      String updatedBy
  ) {
    return ForestClientEntity
        .builder()
        .clientNumber("000")
        .clientStatusCode("ACT")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .revision(1L)
        .createdBy(ApplicationConstant.PROCESSOR_USER_NAME)
        .updatedBy(ApplicationConstant.PROCESSOR_USER_NAME)
        .addOrgUnit(ApplicationConstant.ORG_UNIT)
        .updateOrgUnit(ApplicationConstant.ORG_UNIT)
        .createdBy(createdBy)
        .updatedBy(updatedBy)
        .build();
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
    return legacyR2dbcEntityTemplate
        .selectOne(
            Query
                .empty()
                .sort(Sort.by(Direction.DESC, ApplicationConstant.CLIENT_NUMBER))
                .limit(1),
            ForestClientEntity.class
        )
        .map(ForestClientEntity::getClientNumber)
        .map(lastForestClientNumber -> String.format("%08d",
            Integer.parseInt(lastForestClientNumber) + 1)
        );
  }

  private Mono<Integer> getNextDoingBusinessAs() {
    return legacyR2dbcEntityTemplate
        .selectOne(
            Query
                .empty()
                .sort(Sort.by(Direction.DESC, "CLIENT_DBA_ID"))
                .limit(1),
            ClientDoingBusinessAsEntity.class
        )
        .map(ClientDoingBusinessAsEntity::getId)
        .map(lastId -> lastId + 1);
  }

  private Mono<ClientDoingBusinessAsEntity> createClientDoingBusinessAs(
      Message<ForestClientEntity> message, ForestClientEntity forestClient) {
    return doingBusinessAsRepository
        .existsByClientNumber(forestClient.getClientNumber())
        .filter(not(Boolean::booleanValue))
        .flatMap(doesNotExist -> getNextDoingBusinessAs())
        .map(nextId ->
            ClientDoingBusinessAsEntity
                .builder()
                .id(nextId)
                .clientNumber(forestClient.getClientNumber())
                .doingBusinessAsName(message
                    .getHeaders()
                    .get(ApplicationConstant.FOREST_CLIENT_NAME,
                        String.class)
                )
                .revision(1L)
                .createdBy(ApplicationConstant.PROCESSOR_USER_NAME)
                .createdAt(LocalDateTime.now())
                .updatedBy(ApplicationConstant.PROCESSOR_USER_NAME)
                .updatedAt(LocalDateTime.now())
                .addOrgUnit(ApplicationConstant.ORG_UNIT)
                .updateOrgUnit(ApplicationConstant.ORG_UNIT)
                .build()
        )
        .flatMap(doingBusinessAs ->
            legacyR2dbcEntityTemplate
                .insert(ClientDoingBusinessAsEntity.class)
                .using(doingBusinessAs)
        );
  }

  private boolean isRegisteredSoleProprietorship(ForestClientEntity forestClient) {
    return forestClient
               .getClientTypeCode()
               .equalsIgnoreCase("I")
           &&
           StringUtils.equalsIgnoreCase(
               forestClient
                   .getClientIdTypeCode(),
               "OTHR"
           );
  }

  private Mono<ForestClientLocationEntity> toForestClientLocationEntity(
      long index,
      SubmissionLocationEntity submissionLocation
  ) {
    return Mono.just(ForestClientLocationEntity
        .builder()
        .clientLocnCode(String.format("%02d", index))
        .clientLocnName(submissionLocation.getName().toUpperCase())
        .addressOne(submissionLocation.getStreetAddress())
        .city(submissionLocation.getCityName())
        .province(submissionLocation.getProvinceCode())
        .country(
            countryList
                .getOrDefault(
                    submissionLocation.getCountryCode(),
                    submissionLocation.getCountryCode()
                )
        )
        .postalCode(submissionLocation.getPostalCode())
        .businessPhone(null)
        .homePhone(null)
        .cellPhone(null)
        .faxNumber(null)
        .emailAddress(null)
        .locnExpiredInd("N")
        .returnedMailDate(null)
        .trustLocationInd("Y")
        .cliLocnComment(null)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .revision(1L)
        .createdBy(ApplicationConstant.PROCESSOR_USER_NAME)
        .updatedBy(ApplicationConstant.PROCESSOR_USER_NAME)
        .addOrgUnit(ApplicationConstant.ORG_UNIT)
        .updateOrgUnit(ApplicationConstant.ORG_UNIT)
        .build()
    );
  }

  private String getClientNumber(Message<?> message) {
    return ProcessorUtil
        .readHeader(
            message,
            ApplicationConstant.FOREST_CLIENT_NUMBER,
            String.class
        )
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
