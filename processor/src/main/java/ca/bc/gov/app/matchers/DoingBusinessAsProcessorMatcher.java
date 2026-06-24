package ca.bc.gov.app.matchers;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.dto.legacy.ClientDoingBusinessAsDto;
import ca.bc.gov.app.service.legacy.LegacyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Matches a submission against legacy clients using their registered
 * "Doing Business As" (DBA) name, via fuzzy matching in the legacy system.
 *
 * <p>Only applies to submissions with client type "RSP"; see {@link #enabled}.
 *
 * @see ProcessorMatcher
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DoingBusinessAsProcessorMatcher implements ProcessorMatcher {

  private final LegacyService legacyService;

  /**
   * Enabled only for "RSP" (Responsible Service Provider) submissions, since
   * DBA matching is only meaningful for that client type.
   *
   * @param submission the submission to check
   * @return true if {@code submission.clientType()} is "RSP" (case-insensitive)
   */
  @Override
  public boolean enabled(SubmissionInformationDto submission) {
    return "RSP".equalsIgnoreCase(submission.clientType());
  }

  @Override
  public String name() {
    return "Doing Business As Fuzzy Matcher";
  }

  @Override
  public String fieldName() {
    return ApplicationConstant.MATCH_PARAM_NAME;
  }

  /**
   * Looks up legacy clients whose DBA name matches the submission's
   * corporation name, and collects their client numbers into a result.
   *
   * @param submission the submission being matched; uses {@code corporationName}
   * @return a {@link Mono} emitting a {@link MatcherResult} with the matched
   *     client numbers (empty result if no matches are found)
   */
  @Override
  public Mono<MatcherResult> matches(SubmissionInformationDto submission) {
    log.info("{} :: Validating {}", name(), submission.corporationName());

    return matchBy(submission.corporationName())
        .map(ClientDoingBusinessAsDto::clientNumber)
        .collect(initializeResult(), compileResult());
  }

  /**
   * Queries the legacy system for clients registered under the given DBA name.
   *
   * @param companyName the DBA / corporation name to match against
   * @return a {@link Flux} of matching legacy clients; empty if none are found
   */
  private Flux<ClientDoingBusinessAsDto> matchBy(String companyName) {
    return legacyService
        .matchDba(companyName)
        .doOnNext(entity -> log.info("Found a match {}", entity));
  }
}