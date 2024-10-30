package ca.bc.gov.app.matchers;

import ca.bc.gov.app.dto.submissions.MatcherResult;
import ca.bc.gov.app.dto.submissions.SubmissionInformationDto;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * This class is a component that matches submissions based on the "Good Standing" field.
 * It implements the ProcessorMatcher interface.
 */
@Component
@Slf4j
public class GoodStandingProcessorMatcher implements ProcessorMatcher {

  /**
   * This method checks if the matcher is enabled for a given submission.
   * It always returns true.
   *
   * @param submission A SubmissionInformationDto object.
   * @return A boolean indicating if the matcher is enabled.
   */
  @Override
  public boolean enabled(SubmissionInformationDto submission) {
    return true;
  }

  /**
   * This method returns the name of the matcher.
   *
   * @return A string representing the name of the matcher.
   */
  @Override
  public String name() {
    return "Good Standing Matcher";
  }

  /**
   * This method returns the field name that the matcher operates on.
   *
   * @return A string representing the field name.
   */
  @Override
  public String fieldName() {
    return "goodStanding";
  }

  /**
   * This method performs the matching operation for a given submission.
   * It checks the "Good Standing" field of the submission.
   * If the field is blank, it returns a MatcherResult with a message "Value not found".
   * If the field is "N", it returns a MatcherResult with a message "Client not in good standing".
   * Otherwise, it returns a MatcherResult with an empty set.
   *
   * @param submission A SubmissionInformationDto object.
   * @return A Mono of MatcherResult containing the result of the matching operation.
   */
  @Override
  public Mono<MatcherResult> matches(SubmissionInformationDto submission) {

    log.info("{} :: Validating {}",name(),submission.goodStanding());

    if (StringUtils.isBlank(submission.goodStanding())) {
      return Mono.just(new MatcherResult(fieldName(), Set.of("Value not found")));
    }

    if (StringUtils.equalsIgnoreCase("N", submission.goodStanding())) {
      return Mono.just(new MatcherResult(fieldName(), Set.of("Client not in good standing")));
    }

    return Mono.just(new MatcherResult(fieldName(), Set.of()));
  }
}