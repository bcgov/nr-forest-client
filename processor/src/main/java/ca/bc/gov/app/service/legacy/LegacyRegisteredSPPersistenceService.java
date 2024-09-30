
package ca.bc.gov.app.service.legacy;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.dto.SubmissionProcessTypeEnum;
import ca.bc.gov.app.dto.bcregistry.BcRegistryPartyDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.repository.SubmissionContactRepository;
import ca.bc.gov.app.repository.SubmissionDetailRepository;
import ca.bc.gov.app.repository.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.SubmissionLocationRepository;
import ca.bc.gov.app.repository.SubmissionRepository;
import ca.bc.gov.app.service.bcregistry.BcRegistryService;
import ca.bc.gov.app.util.ProcessorUtil;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;


/**
 * This class is responsible for persisting the submission of registered sole proprietorship into
 * the legacy database.
 */
@Service
@Slf4j
public class LegacyRegisteredSPPersistenceService extends LegacyAbstractPersistenceService {

  private final BcRegistryService bcRegistryService;

  public LegacyRegisteredSPPersistenceService(
      SubmissionDetailRepository submissionDetailRepository,
      SubmissionRepository submissionRepository,
      SubmissionLocationRepository locationRepository,
      SubmissionContactRepository contactRepository,
      SubmissionLocationContactRepository locationContactRepository,
      LegacyService legacyService,
      BcRegistryService bcRegistryService
  ) {
    super(submissionDetailRepository, submissionRepository, locationRepository, contactRepository,
        locationContactRepository, legacyService);
    this.bcRegistryService = bcRegistryService;
  }


  /**
   * This method is responsible for filtering the submission based on the type.
   *
   * @param clientTypeCode - the client type code.
   * @return - true if the type is RSP, otherwise false.
   */
  @Override
  boolean filterByType(String clientTypeCode) {
    return StringUtils.equalsIgnoreCase(clientTypeCode, "RSP");
  }

  @Override
  public Mono<MessagingWrapper<ForestClientDto>> generateForestClient(
      MessagingWrapper<String> message) {

    AtomicReference<String> atomicClientName = new AtomicReference<>(StringUtils.EMPTY);

    return
        getSubmissionDetailRepository()
            .findBySubmissionId(
                (Integer)
                    message
                        .parameters()
                        .get(ApplicationConstant.SUBMISSION_ID)
            )

            //if Staff submitted, get the client name from the submission as doing business as
            .doOnNext(submissionDetail -> {
              atomicClientName.set(
                  Optional
                      .ofNullable(submissionDetail.getDoingBusinessAs())
                      .filter(StringUtils::isNotBlank)
                      .orElse(submissionDetail.getOrganizationName())
              );
            })

            .map(submissionDetail ->
                getBaseForestClient(
                    getUser(message, ApplicationConstant.CREATED_BY),
                    getUser(message, ApplicationConstant.UPDATED_BY)
                )
                    .withBirthdate(submissionDetail.getBirthdate())
                    .withClientIdentification(submissionDetail.getRegistrationNumber())
                    .withClientComment(
                        BooleanUtils.toString(
                            isStaffSubmitted(message),
                            submissionDetail.getNotes(),
                            String.join(" ",
                                getUser(message, ApplicationConstant.CLIENT_SUBMITTER_NAME),
                                "of BCeID Business",
                                message.getInfoParameter("businessName", String.class),
                                "submitted the",
                                "sole proprietor registered on BC Registry with number",
                                submissionDetail.getRegistrationNumber(),
                                "and company name",
                                submissionDetail.getOrganizationName().toUpperCase()
                            )
                        )
                    )
                    .withRegistryCompanyTypeCode(ProcessorUtil.extractLetters(
                        submissionDetail.getRegistrationNumber()))
                    .withCorpRegnNmbr(ProcessorUtil.extractNumbers(
                        submissionDetail.getRegistrationNumber()))
                    .withClientNumber(message.payload())
                    .withAcronym(submissionDetail.getClientAcronym())
                    .withWcbFirmNumber(submissionDetail.getWorkSafeBCNumber())

                    .withLegalFirstName(
                        isStaffSubmitted(message) ? submissionDetail.getFirstName().toUpperCase()
                            : StringUtils.EMPTY)
                    .withClientName(
                        isStaffSubmitted(message) ? submissionDetail.getLastName().toUpperCase()
                            : StringUtils.EMPTY)
                    .withClientTypeCode("I")
                    .withClientIdTypeCode("BCRE")
            )
            //Load the details to set the remaining fields
            .flatMap(forestClient ->
                Mono.just(
                        SubmissionProcessTypeEnum.STAFF.equals(
                            message.getParameter(ApplicationConstant.SUBMISSION_STARTER,
                                SubmissionProcessTypeEnum.class)
                        )
                    )
                    .filter(BooleanUtils::isTrue)
                    .map(isStaff -> forestClient)
                    .switchIfEmpty(loadRSPNameFromContact(message, forestClient))
            )
            .map(forestClient ->
                new MessagingWrapper<>(
                    forestClient,
                    message.parameters()
                )
                    .withParameter(ApplicationConstant.FOREST_CLIENT_NAME,
                            atomicClientName.get().toUpperCase()
                    )
                    .withParameter(ApplicationConstant.REGISTRATION_NUMBER,
                        String.join(StringUtils.EMPTY,
                            forestClient.registryCompanyTypeCode(),
                            forestClient.corpRegnNmbr()
                        )
                    )
                    .withParameter(
                        ApplicationConstant.IS_DOING_BUSINESS_AS,
                        true
                    )
                    .withParameter(
                        ApplicationConstant.DOING_BUSINESS_AS,
                        atomicClientName.get().toUpperCase()
                    )
            );

  }

  private Mono<ForestClientDto> loadRSPNameFromContact(MessagingWrapper<String> message,
      ForestClientDto forestClient) {
    return bcRegistryService
        .requestDocumentData(forestClient.clientIdentification())
        // Should only be one
        .next()
        // Get the proprietor or empty if none
        .flatMap(document ->
            Mono.justOrEmpty(
                Optional.ofNullable(
                    document.getProprietor()
                )
            )
        )
        .map(BcRegistryPartyDto::officer)
        .map(contact ->
            new String[]{
                contact.firstName().toUpperCase(),
                contact.lastName().toUpperCase()
            })
        .switchIfEmpty(
            //In case of negative results from BC Registry (rare)
            // load from contacts as a fallback
            getContactRepository()
                //Get all contacts for the submission
                .findBySubmissionId(
                    (Integer)
                        message
                            .parameters()
                            .get(ApplicationConstant.SUBMISSION_ID)
                )
                //Handle as a flux with the index
                .index()
                //Only deal with the first 2
                .filter(index -> index.getT1() < 2)
                .map(Tuple2::getT2)
                .map(contact -> new String[]{
                    contact.getFirstName().toUpperCase(),
                    contact.getLastName().toUpperCase()
                })
                //Grab the last, as it should cover the case of the second being removed
                .last()
        )
        .map(contact ->
            forestClient
                .withLegalFirstName(contact[0])
                .withClientName(contact[1])
        );
  }

}
