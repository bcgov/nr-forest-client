package ca.bc.gov.app.service.client.matches;

import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.StepMatchEnum;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.service.client.ClientLegacyService;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * <p>This class implements the StepMatcher interface and provides the functionality for matching
 * registered steps. It uses the ClientLegacyService to search for registered companies based on the
 * provided client submission data.</p>
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
public class RegisteredStepMatcher implements StepMatcher {

  private static final String BUSINESS_FIELD_NAME = "businessInformation.businessName";

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
   * This method returns the step matcher enumeration value for registered steps.
   *
   * @return The StepMatchEnum value representing the registered step.
   */
  public StepMatchEnum getStepMatcher() {
    return StepMatchEnum.STEP1REGISTERED;
  }

  /**
   * <p>This method matches the client submission data to the registered step. It performs three
   * searches:</p>
   * <ol>
   * <li>A fuzzy match should happen for the Client name</li>
   * <li>A full match should happen for the Incorporation number</li>
   * <li>A fuzzy match should happen for the Doing Business as</li>
   * <li>A full match should happen for the Client name</li>
   * <li>A full match should happen for the Doing Business as</li>
   * <li>A full match should happen for the Acronym</li>
   * <li>A full match should happen for the combination of First name, Last name and date of birth if the user is a Sole Proprietor</li>
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
        Mono
            .just(dto.businessInformation())
            //If it's a Registered Sole Proprietorship
            .filter(businessInformation -> businessInformation.clientType().equals("RSP"))
            //And we have First and Last name
            .filter(businessInformation -> !StringUtils.isAllBlank(
                    businessInformation.firstName(),
                    businessInformation.lastName()
                )
            )
            //And we have a birthdate
            .filter(businessInformation -> businessInformation.birthdate() != null)
            .flatMapMany(businessInformation ->
                legacyService.searchIndividual(
                    businessInformation.firstName(),
                    businessInformation.lastName(),
                    businessInformation.birthdate(),
                    null
                )
            )
            .doOnNext(client -> log.info("Match found for sole proprietor fuzzy match: {}",
                client.clientNumber())
            );

    Flux<ForestClientDto> clientRegistrationFullMatch =
        legacyService
            .searchLegacy(
                dto.businessInformation().registrationNumber(),
                null,
                null,
                null
            ).doOnNext(client -> log.info("Match found for registration number full match: {}",
                client.clientNumber())
            );

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

    Flux<ForestClientDto> dbaFuzzyMatch =
        legacyService
            .searchGeneric(
                "doingBusinessAs",
                "dbaName",
                dto.businessInformation().doingBusinessAs()
            ).doOnNext(client -> log.info("Match found for doing business as fuzzy match: {}",
                client.clientNumber())
            );

    Flux<ForestClientDto> dbaFullMatch =
        legacyService
            .searchGeneric(
                "doingBusinessAs",
                Map.of(
                    "dbaName", List.of(dto.businessInformation().doingBusinessAs()),
                    "isFuzzy", List.of("false")
                )
            ).doOnNext(client -> log.info("Match found for doing business as full match: {}",
                client.clientNumber())
            );

    return reduceMatchResults(
        Flux.concat(

            //A fuzzy match should happen for the Client name
            processResult(
                clientNameFuzzyMatch,
                BUSINESS_FIELD_NAME,
                true,
                true
            ),

            //A full match should happen for the Client name
            processResult(
                clientNameFullMatch,
                BUSINESS_FIELD_NAME,
                true, //Not actually true, just to force a warning instead
                false
            ),

            //A full match should happen for the Incorporation number
            processResult(
                clientRegistrationFullMatch,
                "businessInformation.registrationNumber",
                false,
                false
            ),

            //A fuzzy match should happen for the Doing Business as
            processResult(
                dbaFuzzyMatch,
                "businessInformation.doingBusinessAs",
                true,
                true
            ),

            //A full match should happen for the Doing Business as
            processResult(
                dbaFullMatch,
                "businessInformation.doingBusinessAs",
                false,
                false
            ),

            //A full match should happen for the Acronym
            processResult(
                clientAcronymFullMatch,
                "businessInformation.clientAcronym",
                false,
                false
            ),

            //A full match should happen for the combination of First name, Last name and date of birth if the user is a Sole Proprietor
            //We point to the businessName as this is the only field the user has access to
            processResult(
                individualFuzzyMatch,
                "businessInformation.individual",
                true,
                true
            )
        )
    );
  }
}
