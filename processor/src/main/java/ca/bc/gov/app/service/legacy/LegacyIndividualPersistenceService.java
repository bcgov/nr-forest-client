
package ca.bc.gov.app.service.legacy;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.dto.SubmissionProcessTypeEnum;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.repository.SubmissionContactRepository;
import ca.bc.gov.app.repository.SubmissionDetailRepository;
import ca.bc.gov.app.repository.SubmissionLocationContactRepository;
import ca.bc.gov.app.repository.SubmissionLocationRepository;
import ca.bc.gov.app.repository.SubmissionRepository;
import ca.bc.gov.app.util.ProcessorUtil;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


/**
 * This class is responsible for persisting the submission of individuals into the legacy database.
 */
@Service
@Slf4j
public class LegacyIndividualPersistenceService extends LegacyAbstractPersistenceService {


  public LegacyIndividualPersistenceService(
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
   * @return - true if the type is I, otherwise false.
   */
  @Override
  boolean filterByType(String clientTypeCode) {
    return StringUtils.equalsIgnoreCase(clientTypeCode, "I");
  }

  /**
   * Generate the individual to be persisted into forest client database.
   */
  @Override
  public Mono<MessagingWrapper<ForestClientDto>> generateForestClient(
      MessagingWrapper<String> message) {
    return
        getSubmissionDetailRepository()
            .findBySubmissionId(
                message.getParameter(ApplicationConstant.SUBMISSION_ID, Integer.class)
            )
            .map(detailEntity ->
                getBaseForestClient(
                    getUser(message, ApplicationConstant.CREATED_BY),
                    getUser(message, ApplicationConstant.UPDATED_BY)
                )
                    .withBirthdate(detailEntity.getBirthdate())
                    .withLegalFirstName(
                        ProcessorUtil
                            .splitName(
                                detailEntity.getFirstName(),
                                detailEntity.getMiddleName(),
                                detailEntity.getLastName(),
                                detailEntity.getOrganizationName()
                            )[1]
                            .toUpperCase()
                    )
                    .withClientName(
                        ProcessorUtil
                            .splitName(
                                detailEntity.getFirstName(),
                                detailEntity.getMiddleName(),
                                detailEntity.getLastName(),
                                detailEntity.getOrganizationName()
                            )[0]
                            .toUpperCase()
                    )
                    .withClientComment(
                        BooleanUtils
                            .toString(
                                SubmissionProcessTypeEnum.STAFF.equals(
                                    message.getParameter(ApplicationConstant.SUBMISSION_STARTER,
                                        SubmissionProcessTypeEnum.class)
                                ),
                                detailEntity.getNotes(),
                                getUser(message, ApplicationConstant.CLIENT_SUBMITTER_NAME) +
                                    " submitted the individual with data acquired from BC Services Card"
                            )
                    )
                    .withClientTypeCode("I")
                    .withClientIdTypeCode(
                        Objects.toString(
                            detailEntity.getIdentificationCode(),
                            ProcessorUtil.getClientIdTypeCode(
                                ProcessorUtil.splitName(
                                    getUser(message, ApplicationConstant.CREATED_BY))[1]
                            )
                        )
                    )
                    .withClientIdentification(
                        "BCSC".equals(detailEntity.getIdentificationCode())
                        ? Objects.toString(
                            detailEntity.getIdentificationCode(),
                            ProcessorUtil.splitName(
                                getUser(message, ApplicationConstant.CREATED_BY)
                            )[0]
                        )
                        : detailEntity.getClientIdentification()
                    )
                    .withClientNumber(message.payload())
                    .withAcronym(detailEntity.getClientAcronym())
            )
            .doOnNext(forestClient -> log.info("forest client generated for individual {}",
                forestClient.clientIdTypeCode() + " " + forestClient.clientIdentification()))
            .map(forestClient ->
                new MessagingWrapper<>(forestClient, message.parameters())
                    .withParameter(ApplicationConstant.FOREST_CLIENT_NAME,
                        String.join(" ",
                            forestClient.legalFirstName(),
                            forestClient.clientName()
                        )
                    )
                    .withParameter(ApplicationConstant.REGISTRATION_NUMBER,
                        "not applicable")
            );
  }


}


