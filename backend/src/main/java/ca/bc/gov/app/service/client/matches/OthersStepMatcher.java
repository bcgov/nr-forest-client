package ca.bc.gov.app.service.client.matches;

import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.StepMatchEnum;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.service.client.ClientLegacyService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@Observed
@RequiredArgsConstructor
public class OthersStepMatcher implements StepMatcher {

  /**
   * The ClientLegacyService used to search for registered companies and other data.
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
   * This method returns the step matcher enumeration value for other steps.
   *
   * @return The StepMatchEnum value representing the other step.
   */
  public StepMatchEnum getStepMatcher() {
    return StepMatchEnum.STEP1OTHERS;
  }

  /**
   * <p>This method matches the client submission data to the all other steps. It performs three
   * searches:</p>
   * <ol>
   * <li>A fuzzy match should happen for the Client name</li>
   * <li>A full match should happen for the Client name</li>
   * <li>A full match should happen for the Acronym</li>
   * </ol>
   * <p>The results of these searches are then processed and reduced to a single result.</p>
   *
   * @param dto The ClientSubmissionDto object containing the client data to be matched.
   * @return A Mono<Void> indicating when the matching process is complete.
   */
  @Override
  public Mono<Void> matchStep(ClientSubmissionDto dto) {
    Flux<ForestClientDto> clientNameFullMatch =
        legacyService
            .searchGeneric(
                "clientName",
                dto.businessInformation().businessName()
            ).doOnNext(client -> log.info("Match found for client name full match: {}",
                client.clientNumber())
            );

    Flux<ForestClientDto> clientNameFuzzyMatch =
        legacyService
            .searchGeneric(
                "match",
                "companyName",
                dto.businessInformation().businessName()
            ).doOnNext(client -> log.info("Match found for client name fuzzy match: {}",
                client.clientNumber())
            );

    Flux<ForestClientDto> clientAcronymFullMatch =
        legacyService
            .searchGeneric(
                "acronym",
                dto.businessInformation().clientAcronym()
            ).doOnNext(client -> log.info("Match found for client acronym full match: {}",
                client.clientNumber())
            );

    return reduceMatchResults(
        Flux.concat(

            //A fuzzy match should happen for the Client name
            processResult(
                clientNameFuzzyMatch,
                "businessInformation.businessName",
                true
            ),

            //A full match should happen for the Client name
            processResult(
                clientNameFullMatch,
                "businessInformation.businessName",
                false
            ),

            //A full match should happen for the Acronym
            processResult(
                clientAcronymFullMatch,
                "businessInformation.clientAcronym",
                false
            )
        )
    );
  }

}
