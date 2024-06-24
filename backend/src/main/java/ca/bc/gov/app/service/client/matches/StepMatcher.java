package ca.bc.gov.app.service.client.matches;

import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.MatchResult;
import ca.bc.gov.app.dto.client.StepMatchEnum;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.exception.DataMatchException;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

public interface StepMatcher {

  Logger getLogger();

  /**
   * This method is used to get the step matcher enumeration value. The implementation of this
   * method will depend on the specific step to be matched.
   *
   * @return The StepMatchEnum value representing the step to be matched.
   */
  StepMatchEnum getStepMatcher();

  /**
   * This method is used to match the client submission data to a specific step. The implementation
   * of this method will depend on the specific step to be matched.
   *
   * @param dto The ClientSubmissionDto object containing the client data to be matched.
   * @return A Mono<Void> indicating when the matching process is complete.
   */
  Mono<Void> matchStep(ClientSubmissionDto dto);

  /**
   * <p>This default method processes the result of a match operation.
   * It takes a Flux of ForestClientDto objects, a field name, and a boolean indicating whether the
   * match is fuzzy as input. It performs the following operations:</p>
   * <ol>
   * <li>Maps each ForestClientDto object to its client number.</li>
   * <li>Collects the client numbers into a string, separated by commas.</li>
   * <li>Maps the string of client numbers to a new MatchResult object,
   * using the provided field name and fuzzy indicator.</li>
   * </ol>
   *
   * @param response  A Flux of ForestClientDto objects to be processed.
   * @param fieldName The name of the field to be used in the MatchResult object.
   * @param isFuzzy   A boolean indicating whether the match is fuzzy.
   * @return A Mono<MatchResult> object containing the result of the match operation.
   */
  default Mono<MatchResult> processResult(
      Flux<ForestClientDto> response,
      String fieldName,
      boolean isFuzzy
  ) {
    return response
        .map(ForestClientDto::clientNumber)
        .collect(Collectors.joining(","))
        .filter(StringUtils::isNotBlank) // filter out empty strings to prevent false positives
        .doOnNext(
            clientNumbers -> getLogger().info("Matched client number(s) [{}] for field {}",
                clientNumbers, fieldName)
        )
        .map(clientNumbers -> new MatchResult(fieldName, clientNumbers, isFuzzy));
  }

  /**
   * <p>This default method is used to reduce match results. If there's any match result, it will
   * throw a DataMatchException, if not, it will return an empty Mono. It takes a Flux of
   * MatchResult objects as input and performs the following operations:</p>
   * <ol>
   * <li>Sorts the MatchResult objects based on the fuzzy attribute.</li>
   * <li>Removes duplicate MatchResult objects based on the field attribute.</li>
   * <li>Collects the remaining MatchResult objects into a list.</li>
   * <li>Maps the list of MatchResult objects to a new DataMatchException object.</li>
   * <li>Returns a Mono error with the DataMatchException.</li>
   * </ol>
   *
   * @param responses A Flux of MatchResult objects to be reduced.
   * @return A Mono<Void> that completes with an error containing the DataMatchException.
   */
  default Mono<Void> reduceMatchResults(
      Flux<MatchResult> responses
  ) {
    return responses
        .sort(Comparator.comparing(MatchResult::fuzzy))
        .distinct(MatchResult::field)
        .collectList()
        .filter(
            matchResults -> !matchResults.isEmpty()
        ) // filter out empty lists to prevent false positives
        .doOnNext(
            matchResults -> getLogger().info("Matched results: {}", matchResults)
        )
        .doFinally(signalType -> {
          if (SignalType.ON_COMPLETE.equals(signalType)) {
            getLogger().info("Matched results processing complete with no matches");
          }
        })
        .map(DataMatchException::new)
        .flatMap(Mono::error);
  }


}
