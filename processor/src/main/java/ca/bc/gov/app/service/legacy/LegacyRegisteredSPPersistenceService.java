
package ca.bc.gov.app.service.legacy;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.bcregistry.BcRegistryPartyDto;
import ca.bc.gov.app.entity.legacy.ForestClientEntity;
import ca.bc.gov.app.repository.client.CountryCodeRepository;
import ca.bc.gov.app.repository.client.SubmissionContactRepository;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import ca.bc.gov.app.repository.legacy.ClientDoingBusinessAsRepository;
import ca.bc.gov.app.service.bcregistry.BcRegistryService;
import ca.bc.gov.app.util.ProcessorUtil;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
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
      R2dbcEntityOperations legacyR2dbcEntityTemplate,
      CountryCodeRepository countryCodeRepository,
      ClientDoingBusinessAsRepository doingBusinessAsRepository,
      BcRegistryService bcRegistryService
  ) {
    super(
        submissionDetailRepository,
        submissionRepository,
        locationRepository,
        contactRepository,
        locationContactRepository,
        legacyR2dbcEntityTemplate,
        countryCodeRepository,
        doingBusinessAsRepository
    );
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
  String getNextChannel() {
    return ApplicationConstant.SUBMISSION_LEGACY_RSP_CHANNEL;
  }

  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_LEGACY_RSP_CHANNEL,
      outputChannel = ApplicationConstant.SUBMISSION_LEGACY_CLIENT_PERSIST_CHANNEL,
      async = "true"
  )
  @Override
  public Mono<Message<ForestClientEntity>> generateForestClient(Message<String> message) {

    return
        getSubmissionDetailRepository()
            .findBySubmissionId(
                message
                    .getHeaders()
                    .get(ApplicationConstant.SUBMISSION_ID, Integer.class)
            )
            .map(submissionDetail ->
                getBaseForestClient(
                    getUser(message, ApplicationConstant.CREATED_BY),
                    getUser(message, ApplicationConstant.UPDATED_BY)
                )
                    .withBirthdate(submissionDetail.getBirthdate())
                    .withClientIdentification(submissionDetail.getIncorporationNumber())
                    .withClientComment(
                        String.join(" ",
                            "Sole proprietorship registered on BC Registry with number",
                            submissionDetail.getIncorporationNumber(),
                            "and company name",
                            submissionDetail.getOrganizationName().toUpperCase()
                        )
                    )
                    .withRegistryCompanyTypeCode(ProcessorUtil.extractLetters(
                        submissionDetail.getIncorporationNumber()))
                    .withCorpRegnNmbr(ProcessorUtil.extractNumbers(
                        submissionDetail.getIncorporationNumber()))
            )
            //Load the details to set the remaining fields
            .flatMap(forestClient ->
                bcRegistryService
                    .requestDocumentData(forestClient.getClientIdentification())
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
                                message
                                    .getHeaders()
                                    .get(ApplicationConstant.SUBMISSION_ID, Integer.class)
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
                            .withClientTypeCode("I")
                            .withClientIdTypeCode("OTHR")
                            .withClientNumber(message.getHeaders()
                                .get(ApplicationConstant.FOREST_CLIENT_NUMBER, String.class))
                    )
            )
            .map(forestClient ->
                MessageBuilder
                    .withPayload(forestClient)
                    .copyHeaders(message.getHeaders())
                    .setHeader(ApplicationConstant.FOREST_CLIENT_NAME,
                        forestClient
                            .getClientComment()
                            .split("and company name ")[1]
                    )
                    .setHeader(ApplicationConstant.INCORPORATION_NUMBER,
                        String.join(StringUtils.EMPTY,
                            forestClient.getRegistryCompanyTypeCode(),
                            forestClient.getCorpRegnNmbr()
                        )
                    )
                    .build()
            );

  }

}
