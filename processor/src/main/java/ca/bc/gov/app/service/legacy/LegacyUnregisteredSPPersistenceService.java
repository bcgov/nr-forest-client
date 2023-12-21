
package ca.bc.gov.app.service.legacy;

import ca.bc.gov.app.ApplicationConstant;
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
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
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

  @Override
  String getNextChannel() {
    return ApplicationConstant.SUBMISSION_LEGACY_USP_CHANNEL;
  }

  /**
   * This method is responsible for generating the forest client for unregistered sole
   * proprietorship.
   *
   * @param message - the message containing the submission id.
   * @return - the forest client.
   */
  @ServiceActivator(
      inputChannel = ApplicationConstant.SUBMISSION_LEGACY_USP_CHANNEL,
      outputChannel = ApplicationConstant.SUBMISSION_LEGACY_CLIENT_PERSIST_CHANNEL,
      async = "true"
  )
  @Override
  public Mono<Message<ForestClientDto>> generateForestClient(Message<String> message) {
    return
        getSubmissionDetailRepository()
            .findBySubmissionId(
                message
                    .getHeaders()
                    .get(ApplicationConstant.SUBMISSION_ID, Integer.class)
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
                    .withClientNumber(message.getHeaders()
                        .get(ApplicationConstant.FOREST_CLIENT_NUMBER, String.class))
            )
            .doOnNext(forestClient ->
                log.info("generated forest client for USP {} {}",
                    forestClient.clientName(),
                    message.getHeaders().get(ApplicationConstant.FOREST_CLIENT_NUMBER, String.class)
                )
            )
            .map(forestClient ->
                MessageBuilder
                    .withPayload(forestClient)
                    .copyHeaders(message.getHeaders())
                    .setHeader(ApplicationConstant.FOREST_CLIENT_NAME,
                        Stream.of(
                                forestClient.legalFirstName(),
                                forestClient.legalMiddleName(),
                                forestClient.clientName()
                            )
                            .filter(StringUtils::isNotBlank)
                            .collect(Collectors.joining(" "))
                    )
                    .setHeader(ApplicationConstant.INCORPORATION_NUMBER,
                        "not applicable")
                    .build()
            );
  }


}
