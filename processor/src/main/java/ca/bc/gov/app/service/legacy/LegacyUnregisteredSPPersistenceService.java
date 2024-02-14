
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


/**
 * This class is responsible for persisting the submission of unregistered sole proprietorship into
 * the legacy database.
 */
@Service
@Slf4j
public class LegacyUnregisteredSPPersistenceService extends LegacyAbstractPersistenceService {


  public LegacyUnregisteredSPPersistenceService(
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
   * @return - true if the type is USP, otherwise false.
   */
  @Override
  boolean filterByType(String clientTypeCode) {
    return StringUtils.equalsIgnoreCase(clientTypeCode, "USP");
  }

  /**
   * This method is responsible for generating the forest client for unregistered sole
   * proprietorship.
   *
   * @param message - the message containing the submission id.
   * @return - the forest client.
   */
  @Override
  public Mono<MessagingWrapper<ForestClientDto>> generateForestClient(
      MessagingWrapper<String> message) {
    return
        getSubmissionDetailRepository()
            .findBySubmissionId(
                (Integer)
                    message
                        .parameters()
                        .get(ApplicationConstant.SUBMISSION_ID)
            )
            .map(detail ->
                getBaseForestClient(
                    getUser(message, ApplicationConstant.CREATED_BY),
                    getUser(message, ApplicationConstant.UPDATED_BY)
                )
                    .withBirthdate(detail.getBirthdate())
                    .withLegalFirstName(
                        ProcessorUtil.splitName(detail.getOrganizationName())[1].toUpperCase())
                    .withClientName(
                        ProcessorUtil.splitName(detail.getOrganizationName())[0].toUpperCase())
                    .withLegalMiddleName(
                        ProcessorUtil.splitName(detail.getOrganizationName())[2].toUpperCase())
                    .withClientComment(
                        getUser(message, ApplicationConstant.CLIENT_SUBMITTER_NAME) +
                        " submitted the sole proprietor with data acquired from Business BCeID")
                    .withClientTypeCode("I")
                    .withClientIdTypeCode(
                        ProcessorUtil.getClientIdTypeCode(
                            ProcessorUtil.splitName(
                                getUser(message, ApplicationConstant.CREATED_BY))[1]
                        )
                    )
                    .withClientIdentification(
                        ProcessorUtil.splitName(
                            getUser(message, ApplicationConstant.CREATED_BY))[0]
                    )
                    .withClientNumber(message.payload())
            )
            .doOnNext(forestClient ->
                log.info("Generated forest client for USP {}",
                    forestClient.clientName()
                )
            )
            .map(forestClient ->
                new MessagingWrapper<>(forestClient, message.parameters())
                    .withParameter(ApplicationConstant.FOREST_CLIENT_NAME,
                        Stream.of(
                                forestClient.legalFirstName(),
                                forestClient.legalMiddleName(),
                                forestClient.clientName()
                            )
                            .filter(StringUtils::isNotBlank)
                            .collect(Collectors.joining(" "))
                    )
                    .withParameter(ApplicationConstant.REGISTRATION_NUMBER,
                        "not applicable")
            );
  }


}
