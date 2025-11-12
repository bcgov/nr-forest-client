package ca.bc.gov.app.service.client.matches;

import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.IdentificationTypeEnum;
import ca.bc.gov.app.dto.client.StepMatchEnum;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.service.client.ClientLegacyService;
import io.micrometer.observation.annotation.Observed;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * <p>This class implements the StepMatcher interface and provides the functionality for matching
 * individual steps. It uses the ClientLegacyService to search for individuals and documents based
 * on the provided client submission data.</p>
 *
 * <p>Change (FSADT1-2043)</p>
 * The logic for determining the match is changed to be the following:
 *
 * <p><b>IF</b> (ID has 100% match) <b>THEN</b></p>
 * &nbsp;&nbsp;<span>Red banner, cannot proceed. <b>Client Exist</b></span>
 *
 * <p><b>ELSE IF</b> (name has 100% match) <b>THEN</b></p>
 * &nbsp;&nbsp;<span>Yellow warning, <b>can proceed</b></span>
 *
 * <p><b>ELSE</b> (name fuzzy matching) <b>THEN</b></p>
 * &nbsp;&nbsp;<span>Yellow warning, <b>can proceed</b></span>
 */
@Component
@Slf4j
@Observed
@RequiredArgsConstructor
public class IndividualStepMatcher implements StepMatcher {

  /**
   * The ClientLegacyService used to search for individuals and documents.
   */
  private final ClientLegacyService legacyService;

  /**
   * This method is used to get the logger for this class. This is just to allow the default methods
   * to access the logger.
   *
   * @return The Logger object used for logging in this class.
   */
  public Logger getLogger() {
    return log;
  }

  /**
   * This method returns the step matcher enumeration value for individual steps.
   *
   * @return The StepMatchEnum value representing the individual step.
   */
  @Override
  public StepMatchEnum getStepMatcher() {
    return StepMatchEnum.STEP1INDIVIDUAL;
  }

  /**
   * <p>This method matches the client submission data to the individual step. It performs three
   * searches:</p>
   * <ol>
   * <li>A fuzzy search for individuals without a document ID.</li>
   * <li>A full search for individuals with a document ID.</li>
   * <li>A full search for the document itself.</li>
   * </ol>
   * <p>The results of these searches are then processed and reduced to a single result.</p>
   *
   * @param dto The ClientSubmissionDto object containing the client data to be matched.
   * @return A Mono<Void> indicating when the matching process is complete.
   */
  @Override
  public Mono<Void> matchStep(ClientSubmissionDto dto) {

    // Search for individual without document id
    Flux<ForestClientDto> individualFuzzyMatch =
        legacyService
            .searchIndividual(
                dto.businessInformation().firstName(),
                dto.businessInformation().lastName(),
                dto.businessInformation().birthdate(),
                null
            )
            .doOnNext(client -> log.info("Match found for individual fuzzy match: {}",
                client.clientNumber())
            );

    // Search for individual with document id
    Flux<ForestClientDto> individualFullMatch =
        legacyService
            .searchIndividual(
                dto.businessInformation().firstName(),
                dto.businessInformation().lastName(),
                dto.businessInformation().birthdate(),
                dto.businessInformation().clientIdentification()
            )
            .doOnNext(client -> log.info("Match found for individual full match: {}",
                client.clientNumber())
            );

    // Search for document itself
    Flux<ForestClientDto> documentFullMatch =
        Mono
            .justOrEmpty(dto.businessInformation().idType())
            .filter(Objects::nonNull)
            .filter(idType -> !IdentificationTypeEnum.OTHR.name().equalsIgnoreCase(idType))
            .flatMapMany(idType ->
                legacyService.searchDocument(idType,
                    dto.businessInformation().clientIdentification())
            )
            .doOnNext(client -> log.info("Match found for individual document full match: {}",
                client.clientNumber())
            );

    return
        reduceMatchResults(
            Flux
                .concat(
                    processResult(
                        individualFuzzyMatch,
                        //using this name for reference only to denote that is only the individual
                        //information that was matched
                        "businessInformation.individual",
                        true,
                        true
                    ),
                    processResult(
                        individualFullMatch,
                        //using this name for reference only to denote that is the individual data
                        //plus the document id that was matched
                        "businessInformation.individualAndDocument",
                        true, // Not actually fuzzy, but we want to treat it as such
                        false
                    ),
                    processResult(
                        documentFullMatch,
                        "businessInformation.clientIdentification",
                        false,
                        false
                    )
                )
        );
  }
}
