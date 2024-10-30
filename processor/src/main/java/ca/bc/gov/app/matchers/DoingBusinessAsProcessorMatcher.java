package ca.bc.gov.app.matchers;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.legacy.ClientDoingBusinessAsDto;
import ca.bc.gov.app.dto.submissions.MatcherResult;
import ca.bc.gov.app.dto.submissions.SubmissionInformationDto;
import ca.bc.gov.app.service.legacy.LegacyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This class is a component that matches submissions based on the "Doing Business As" field.
 * It implements the ProcessorMatcher interface.
 * It uses a LegacyService to interact with the legacy system.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DoingBusinessAsProcessorMatcher implements ProcessorMatcher {

  private final LegacyService legacyService;

  /**
   * This method checks if the matcher is enabled for a given submission.
   * It returns true if the client type of the submission is "RSP".
   *
   * @param submission A SubmissionInformationDto object.
   * @return A boolean indicating if the matcher is enabled.
   */
  @Override
  public boolean enabled(SubmissionInformationDto submission) {
    return "RSP".equalsIgnoreCase(submission.clientType());
  }

  /**
   * This method returns the name of the matcher.
   *
   * @return A string representing the name of the matcher.
   */
  @Override
  public String name() {
    return "Doing Business As Fuzzy Matcher";
  }

  /**
   * This method returns the field name that the matcher operates on.
   *
   * @return A string representing the field name.
   */
  @Override
  public String fieldName() {
    return ApplicationConstant.MATCH_PARAM_NAME;
  }

  /**
   * This method performs the matching operation for a given submission.
   * It retrieves the "Doing Business As" name for the submission and sends a request to the legacy service to find matches.
   * It then collects the client numbers of the matches into a MatcherResult.
   *
   * @param submission A SubmissionInformationDto object.
   * @return A Mono of MatcherResult containing the client numbers of the matches.
   */
  @Override
  public Mono<MatcherResult> matches(SubmissionInformationDto submission) {

    log.info("{} :: Validating {}", name(), submission.corporationName());

    return
        matchBy(submission.corporationName())
            .map(ClientDoingBusinessAsDto::clientNumber)
            .collect(initializeResult(), compileResult());
  }

  /**
   * This method sends a request to the legacy service to find matches for a given company name.
   * It logs each match found.
   *
   * @param companyName A string representing the company name.
   * @return A Flux of ClientDoingBusinessAsDto objects representing the matches.
   */
  private Flux<ClientDoingBusinessAsDto> matchBy(String companyName) {
    return
        legacyService
            .matchDba(companyName)
            .doOnNext(entity -> log.info("Found a match {}", entity));

  }

}