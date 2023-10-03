
package ca.bc.gov.app.service.legacy;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.EmailRequestDto;
import ca.bc.gov.app.entity.client.SubmissionContactEntity;
import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
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
import ca.bc.gov.app.repository.legacy.ForestClientRepository;
import ca.bc.gov.app.util.ProcessorUtil;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class LegacyPersistenceService {

  private final SubmissionDetailRepository submissionDetailRepository;
  private final SubmissionRepository submissionRepository;
  private final SubmissionLocationRepository locationRepository;
  private final ForestClientRepository forestClientRepository;
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
            .setHeader("createdBy", submission.getCreatedBy())
            .setHeader("updatedBy", submission.getUpdatedBy())
            .build()
        );
  }


  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_LEGACY_CLIENT_CHANNEL,
      outputChannel = ApplicationConstant.SUBMISSION_LEGACY_LOCATION_CHANNEL,
      async = "true"
  )
  public Mono<Message<Integer>> createForestClient(Message<Integer> message) {
    return
        submissionDetailRepository
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
            .map(this::toForestClientEntity)
            .doOnNext(forestClient -> forestClient.setCreatedBy(getUser(message, "createdBy")))
            .doOnNext(forestClient -> forestClient.setUpdatedBy(getUser(message, "updatedBy")))
            .flatMap(forestClientRepository::save)
            .map(ForestClientEntity::getClientNumber)
            .doOnNext(forestClientNumber ->
                log.info(
                    "Saved forest client {} {}",
                    message.getPayload(),
                    forestClientNumber
                )
            )
            .flatMap(forestClientNumber ->
                submissionDetailRepository
                    .findBySubmissionId(message.getPayload())
                    .map(submissionDetail -> submissionDetail.withClientNumber(forestClientNumber))
                    .flatMap(submissionDetailRepository::save)
                    .map(forestClientDetail ->
                        MessageBuilder
                            .fromMessage(message)
                            .setHeader("forestClientNumber", forestClientNumber)
                            .setHeader("forestClientName", forestClientDetail.getOrganizationName())
                            .setHeader("incorporationNumber",
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

    return data
        .index((index, detail) ->
            this.toForestClientLocationEntity(detail, index)
                .doOnNext(submissionLocation ->
                    log.info(
                        "Loaded submission location for persistence on oracle {} {} {}",
                        message.getPayload(),
                        submissionLocation.getClientLocnCode(),
                        submissionLocation.getClientLocnName()
                    )
                )
                .doOnNext(forestClient -> forestClient.setCreatedBy(getUser(message, "createdBy")))
                .doOnNext(forestClient -> forestClient.setUpdatedBy(getUser(message, "updatedBy")))
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
                )
                .flatMap(forestClient ->
                    data
                        .count()
                        .map(count ->
                            MessageBuilder
                                .fromMessage(message)
                                .setHeader("locationCode", forestClient.getClientLocnCode())
                                .setHeader("locationId", detail.getSubmissionLocationId())
                                .setHeader("total", count)
                                .setHeader("index", index)
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

    return locationContactRepository
        .findBySubmissionLocationId(message.getHeaders().get("locationId", Integer.class))
        .flatMap(locationContact ->
            contactRepository
                .findById(locationContact.getSubmissionContactId())
                .doOnNext(submissionContact ->
                    log.info(
                        "Loaded submission contact for persistence on oracle {} {} {} {}",
                        message.getPayload(),
                        submissionContact.getFirstName(),
                        submissionContact.getLastName(),
                        message.getHeaders().get("locationCode", String.class)
                    )
                )
                .map(this::toForestClientContactEntity)
                .doOnNext(forestClient -> forestClient.setCreatedBy(getUser(message, "createdBy")))
                .doOnNext(forestClient -> forestClient.setUpdatedBy(getUser(message, "updatedBy")))
                .doOnNext(forestClient -> forestClient.setClientNumber(getClientNumber(message)))
                .doOnNext(forestClientContact -> forestClientContact.setClientLocnCode(
                    message.getHeaders().get("locationCode", String.class)
                    )
                )
                .flatMap(forestClientContactRepository::save)
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
                    message.getHeaders().get("forestClientNumber", String.class),
                    message.getHeaders().get("forestClientName", String.class)
                )
            )
            .map(submissionContact ->
                new EmailRequestDto(
                    message.getHeaders().get("incorporationNumber", String.class),
                    message.getHeaders().get("forestClientName", String.class),
                    submissionContact.getUserId(),
                    submissionContact.getFirstName(),
                    submissionContact.getEmailAddress(),
                    "approval",
                    "Success",
                    Map.of(
                        "userName", submissionContact.getFirstName(),
                        "business", Map.of(
                            "name", message.getHeaders().get("forestClientName", String.class),
                            "clientNumber",
                            message.getHeaders().get("forestClientNumber", String.class)
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
        .clientNumber(null)
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
        .createdBy("SYSTEM")
        .updatedBy("SYSTEM")
        .addOrgUnit(70L)
        .updateOrgUnit(70L)
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
        .createdBy("SYSTEM")
        .updatedBy("SYSTEM")
        .addOrgUnit(70L)
        .updateOrgUnit(70L)
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
        .createdBy("SYSTEM")
        .updatedBy("SYSTEM")
        .addOrgUnit(70L)
        .updateOrgUnit(70L)
        .build();
  }

  private String getUser(Message<?> message, String headerName) {
    return ProcessorUtil
        .readHeader(
            message,
            headerName,
            String.class
        )
        .orElse("AUTO-PROCESSOR");
  }

  private String getClientNumber(Message<?> message) {
    return ProcessorUtil
        .readHeader(
            message,
            "forestClientNumber",
            String.class
        )
        .orElse(StringUtils.EMPTY);
  }

}


