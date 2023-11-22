
package ca.bc.gov.app.service.legacy;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.entity.legacy.ForestClientEntity;
import ca.bc.gov.app.repository.client.CountryCodeRepository;
import ca.bc.gov.app.repository.client.SubmissionContactRepository;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.client.SubmissionLocationRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import ca.bc.gov.app.repository.legacy.ClientDoingBusinessAsRepository;
import ca.bc.gov.app.util.ProcessorUtil;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


/**
 * This class is responsible for persisting the submission into the legacy database.
 */
@Service
@Slf4j
public class LegacyClientPersistenceService extends LegacyAbstractPersistenceService {

  public LegacyClientPersistenceService(
      SubmissionDetailRepository submissionDetailRepository,
      SubmissionRepository submissionRepository,
      SubmissionLocationRepository locationRepository,
      SubmissionContactRepository contactRepository,
      SubmissionLocationContactRepository locationContactRepository,
      R2dbcEntityOperations legacyR2dbcEntityTemplate,
      CountryCodeRepository countryCodeRepository,
      ClientDoingBusinessAsRepository doingBusinessAsRepository
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
  }

  /**
   * This method is responsible for filtering the submission based on the type.
   *
   * @param clientTypeCode - the client type code.
   * @return - true if the type is not RSP, USP or I, otherwise false.
   */
  @Override
  boolean filterByType(String clientTypeCode) {
    return !List.of("RSP", "USP", "I").contains(clientTypeCode);
  }

  @Override
  String getNextChannel() {
    return ApplicationConstant.SUBMISSION_LEGACY_OTHER_CHANNEL;
  }

  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_LEGACY_OTHER_CHANNEL,
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
                    .withClientComment(
                        "Client details acquired from BC Registry " +
                        submissionDetail.getIncorporationNumber()
                    )
                    .withClientName(submissionDetail.getOrganizationName().toUpperCase())
                    .withClientTypeCode(submissionDetail.getClientTypeCode())
                    .withRegistryCompanyTypeCode(
                        ProcessorUtil.extractLetters(submissionDetail.getIncorporationNumber())
                    )
                    .withCorpRegnNmbr(
                        ProcessorUtil.extractNumbers(submissionDetail.getIncorporationNumber())
                    )
                    .withClientNumber(message.getHeaders()
                        .get(ApplicationConstant.FOREST_CLIENT_NUMBER, String.class))
            )
            .map(forestClient ->
                MessageBuilder
                    .withPayload(forestClient)
                    .copyHeaders(message.getHeaders())
                    .setHeader(ApplicationConstant.FOREST_CLIENT_NAME,
                        forestClient.getClientName()
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
