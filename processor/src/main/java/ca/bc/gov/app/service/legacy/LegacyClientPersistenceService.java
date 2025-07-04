
package ca.bc.gov.app.service.legacy;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.repository.SubmissionContactRepository;
import ca.bc.gov.app.repository.SubmissionDetailRepository;
import ca.bc.gov.app.repository.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.SubmissionLocationRepository;
import ca.bc.gov.app.repository.SubmissionRepository;
import ca.bc.gov.app.util.ProcessorUtil;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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
      LegacyService legacyService
  ) {
    super(submissionDetailRepository, submissionRepository, locationRepository, contactRepository,
        locationContactRepository, legacyService);
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
            .doOnNext(submissionDetail ->
                atomicClientName.set(submissionDetail.getDoingBusinessAs())
            )
            .map(submissionDetail ->
                getBaseForestClient(
                    getUser(message, ApplicationConstant.CREATED_BY),
                    getUser(message, ApplicationConstant.UPDATED_BY)
                )
                    .withClientComment(
                        BooleanUtils.toString(
                            isStaffSubmitted(message),
                            submissionDetail.getNotes(),
                            getUser(message, ApplicationConstant.CLIENT_SUBMITTER_NAME) +
                                " submitted the client details acquired from BC Registry " +
                                submissionDetail.getRegistrationNumber()
                        )
                    )
                    .withClientName(submissionDetail.getOrganizationName().toUpperCase(Locale.ROOT))
                    .withClientTypeCode(submissionDetail.getClientTypeCode())
                    .withRegistryCompanyTypeCode(
                        ProcessorUtil.extractLetters(submissionDetail.getRegistrationNumber())
                    )
                    .withCorpRegnNmbr(
                        ProcessorUtil.extractNumbers(submissionDetail.getRegistrationNumber())
                    )
                    .withClientNumber(message.payload())
                    .withAcronym(submissionDetail.getClientAcronym())
                    .withWcbFirmNumber(submissionDetail.getWorkSafeBCNumber())
            )
            .map(forestClient ->
                new MessagingWrapper<>(
                    forestClient,
                    message.parameters()
                )
                    .withParameter(ApplicationConstant.FOREST_CLIENT_NAME,
                        forestClient.clientName()
                    )
                    .withParameter(ApplicationConstant.REGISTRATION_NUMBER,
                        String.join(StringUtils.EMPTY,
                            forestClient.registryCompanyTypeCode(),
                            forestClient.corpRegnNmbr()
                        )
                    )
                    .withParameter(ApplicationConstant.IS_DOING_BUSINESS_AS,
                        StringUtils.isNotBlank(atomicClientName.get())
                    )
                    .withParameter(ApplicationConstant.DOING_BUSINESS_AS,
                        StringUtils.defaultString(atomicClientName.get())
                    )
            );

  }

}
