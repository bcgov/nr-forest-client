
package ca.bc.gov.app.service.legacy;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.EmailRequestDto;
import ca.bc.gov.app.entity.client.SubmissionContactEntity;
import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationContactEntity;
import ca.bc.gov.app.entity.client.SubmissionLocationEntity;
import ca.bc.gov.app.entity.legacy.ForestClientContactEntity;
import ca.bc.gov.app.entity.legacy.ForestClientEntity;
import ca.bc.gov.app.entity.legacy.ForestClientLocationEntity;
import ca.bc.gov.app.repository.client.SubmissionContactRepository;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import ca.bc.gov.app.repository.legacy.ForestClientContactRepository;
import ca.bc.gov.app.util.ProcessorUtil;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class LegacyPersistenceService {

  private final SubmissionDetailRepository submissionDetailRepository;
  private final SubmissionRepository submissionRepository;
  private final SubmissionLocationRepository locationRepository;
  private final ForestClientContactRepository forestClientContactRepository;
  private final SubmissionContactRepository contactRepository;
  private final SubmissionLocationContactRepository locationContactRepository;
  private final R2dbcEntityTemplate legacyR2dbcEntityTemplate;


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
            .withPayload(message.getPayload())
            .setHeader(ApplicationConstant.SUBMISSION_ID, message.getPayload())
            .setHeader(ApplicationConstant.CREATED_BY, submission.getCreatedBy())
            .setHeader(ApplicationConstant.UPDATED_BY, submission.getUpdatedBy())
            .build()
        );
  }


  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_LEGACY_CLIENT_CHANNEL,
      outputChannel = ApplicationConstant.SUBMISSION_LEGACY_LOCATION_CHANNEL,
      async = "true"
  )
  public Mono<Message<Integer>> createForestClient(Message<Integer> message) {

    // Load the details of the submission
    Mono<SubmissionDetailEntity> submission = submissionDetailRepository
        .findBySubmissionId(message.getPayload())
        .doOnNext(
            submissionDetail ->
                log.info(
                    "Loaded submission detail for persistence on oracle {} {} {}",
                    message.getPayload(),
                    submissionDetail.getOrganizationName(),
                    submissionDetail.getIncorporationNumber()
                )
        );

    // Checks if the client number exists for that submission
    Mono<String> clientExists = submission
        .flatMap(submissionDetail -> Mono.justOrEmpty(
                Optional.ofNullable(submissionDetail.getClientNumber())
            )
        )
        .doOnNext(clientNumber ->
            log.info(
                "Client number {} exists for submission {}",
                clientNumber,
                message.getPayload()
            )
        );

    // Convert the submission into a forest client entity
    Mono<ForestClientEntity> client = submission
        .map(this::toForestClientEntity)
        .doOnNext(forestClient -> forestClient.setCreatedBy(getUser(message,
            ApplicationConstant.CREATED_BY)))
        .doOnNext(forestClient -> forestClient.setUpdatedBy(getUser(message,
            ApplicationConstant.UPDATED_BY)));

    // Grabs the next forest client number for insertion
    Mono<String> nextClientNumber = legacyR2dbcEntityTemplate
        .selectOne(
            Query
                .empty()
                .sort(Sort.by(Direction.DESC, "CLIENT_NUMBER"))
                .limit(1),
            ForestClientEntity.class
        )
        .map(ForestClientEntity::getClientNumber)
        .map(lastForestClientNumber -> String.format("%08d",
            Integer.parseInt(lastForestClientNumber) + 1)
        );

    // Saves the forest client entity and gets the client number and updates on the database
    Mono<String> savedClient =
        nextClientNumber
            .flatMap(clientNumber ->
                client
                    .doOnNext(forestClient -> forestClient.setClientNumber(clientNumber))
                    .flatMap(forestClient ->
                        legacyR2dbcEntityTemplate
                            .insert(ForestClientEntity.class)
                            .using(forestClient)
                    )
            )
            .map(ForestClientEntity::getClientNumber)
            .flatMap(clientNumber ->
                submissionDetailRepository
                    .findBySubmissionId(message.getPayload())
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
            );

    return
        // If the clients number already exists, move on, else do the save
        clientExists
            .switchIfEmpty(savedClient)
            // Get the details and prepare the message for next step
            .flatMap(forestClientNumber ->
                submission
                    .map(forestClientDetail ->
                        MessageBuilder
                            .fromMessage(message)
                            .setHeader(ApplicationConstant.FOREST_CLIENT_NUMBER, forestClientNumber)
                            .setHeader(ApplicationConstant.FOREST_CLIENT_NAME,
                                forestClientDetail.getOrganizationName())
                            .setHeader(ApplicationConstant.INCORPORATION_NUMBER,
                                forestClientDetail.getIncorporationNumber())
                            .build()
                    )
            );
  }

  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_LEGACY_LOCATION_CHANNEL,
      outputChannel = ApplicationConstant.SUBMISSION_LEGACY_CONTACT_CHANNEL,
      async = "true"
  )
  public Flux<Message<Integer>> createLocations(Message<Integer> message) {

    Flux<SubmissionLocationEntity> data = locationRepository.findBySubmissionId(
        message.getPayload()
    );

    BiFunction<String, String, Mono<ForestClientLocationEntity>> locateClientLocation = (clientNumber, locationCode) ->
        legacyR2dbcEntityTemplate
            .selectOne(
                Query.query(
                    Criteria
                        .where("CLIENT_LOCN_CODE").is(locationCode)
                        .and("CLIENT_NUMBER").is(clientNumber)
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
        toForestClientLocationEntity(detail, index)
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

  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_LEGACY_CONTACT_CHANNEL,
      outputChannel = ApplicationConstant.SUBMISSION_LEGACY_AGGREGATE_CHANNEL,
      async = "true"
  )
  public Mono<Message<Integer>> createContact(Message<Integer> message) {

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
                                        .where("CLIENT_NUMBER").is(getClientNumber(message))
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
    Mono<String> nextContactId = legacyR2dbcEntityTemplate
        .selectOne(
            Query
                .empty()
                .sort(Sort.by(Direction.DESC, "CLIENT_CONTACT_ID"))
                .limit(1),
            ForestClientContactEntity.class
        )
        .map(ForestClientContactEntity::getClientContactId)
        .map(lastForestClientContactId -> String.valueOf(
            Integer.parseInt(lastForestClientContactId) + 1));

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
    Function<SubmissionLocationContactEntity, Mono<ForestClientContactEntity>> createContact = locationContact ->
        toContact
            .apply(locationContact.getSubmissionContactId())
            .flatMap(forestClientContact ->
                nextContactId
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
        .flatMap(locationContact ->
            forestContact
                .apply(locationContact.getSubmissionContactId())
                .switchIfEmpty(createContact.apply(locationContact))
        )
        .collectList()
        .thenReturn(message);
  }

  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_LEGACY_NOTIFY_CHANNEL,
      outputChannel = ApplicationConstant.SUBMISSION_COMPLETION_CHANNEL,
      async = "true"
  )
  public Mono<Message<EmailRequestDto>> sendNotification(Message<Integer> message) {

    return
        contactRepository
            .findFirstBySubmissionId(message.getPayload())
            .doOnNext(submissionContact ->
                log.info(
                    "All data saved onto oracle {} {} {}",
                    message.getPayload(),
                    message.getHeaders()
                        .get(ApplicationConstant.FOREST_CLIENT_NUMBER, String.class),
                    message.getHeaders().get(ApplicationConstant.FOREST_CLIENT_NAME, String.class)
                )
            )
            .map(submissionContact ->
                new EmailRequestDto(
                    message.getHeaders()
                        .get(ApplicationConstant.INCORPORATION_NUMBER, String.class),
                    message.getHeaders().get(ApplicationConstant.FOREST_CLIENT_NAME, String.class),
                    submissionContact.getUserId(),
                    submissionContact.getFirstName(),
                    submissionContact.getEmailAddress(),
                    "approval",
                    "Success",
                    Map.of(
                        "userName", submissionContact.getFirstName(),
                        "business", Map.of(
                            "name", Objects.requireNonNull(message.getHeaders()
                                .get(ApplicationConstant.FOREST_CLIENT_NAME, String.class)),
                            "clientNumber",
                            Objects.requireNonNull(message.getHeaders()
                                .get(ApplicationConstant.FOREST_CLIENT_NUMBER, String.class))
                        )
                    )
                )
            )
            .map(emailRequestDto ->
                MessageBuilder
                    .withPayload(emailRequestDto)
                    .copyHeaders(message.getHeaders())
                    .build()
            );

  }


  private ForestClientEntity toForestClientEntity(
      SubmissionDetailEntity submissionDetail
  ) {
    return ForestClientEntity
        .builder()
        .clientNumber("000")
        .legalFirstName(null)
        .legalMiddleName(null)
        .clientIdTypeCode(null)
        .clientIdentification(null)
        .clientAcronym(null)
        .wcbFirmNumber(null)
        .ocgSupplierNmbr(null)
        .clientComment(null)
        .clientStatusCode("ACT")
        .clientName(submissionDetail.getOrganizationName())
        .clientTypeCode(submissionDetail.getClientTypeCode())
        .registryCompanyTypeCode(
            ProcessorUtil.extractLetters(submissionDetail.getIncorporationNumber()))
        .corpRegnNmbr(ProcessorUtil.extractNumbers(submissionDetail.getIncorporationNumber()))
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .revision(1L)
        .createdBy(ApplicationConstant.PROCESSOR_USER_NAME)
        .updatedBy(ApplicationConstant.PROCESSOR_USER_NAME)
        .addOrgUnit(ApplicationConstant.ORG_UNIT)
        .updateOrgUnit(ApplicationConstant.ORG_UNIT)
        .build();
  }

  private Mono<ForestClientLocationEntity> toForestClientLocationEntity(
      SubmissionLocationEntity submissionLocation,
      long index
  ) {
    return Mono.just(ForestClientLocationEntity
        .builder()
        .clientLocnCode(String.format("%02d", index))
        .clientLocnName(submissionLocation.getName())
        .addressOne(submissionLocation.getStreetAddress())
        .city(submissionLocation.getCityName())
        .province(submissionLocation.getProvinceCode())
        .country(submissionLocation.getCountryCode()) //TODO: Read the country from the table -_-
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

  private ForestClientContactEntity toForestClientContactEntity(
      SubmissionContactEntity submissionContact
  ) {
    return ForestClientContactEntity
        .builder()
        .contactCode(submissionContact.getContactTypeCode())
        .contactName(String.format("%s %s", submissionContact.getFirstName(),
            submissionContact.getLastName()))
        .businessPhone(RegExUtils.replaceAll(submissionContact.getBusinessPhoneNumber(), "\\D",
            StringUtils.EMPTY))
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

  private String getUser(Message<?> message, String headerName) {
    return ProcessorUtil
        .readHeader(
            message,
            headerName,
            String.class
        )
        .orElse(ApplicationConstant.PROCESSOR_USER_NAME);
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

}


