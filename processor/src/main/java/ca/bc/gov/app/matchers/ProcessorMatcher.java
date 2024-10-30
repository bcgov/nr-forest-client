package ca.bc.gov.app.matchers;

import ca.bc.gov.app.dto.submissions.MatcherResult;
import ca.bc.gov.app.dto.submissions.SubmissionInformationDto;
import java.util.LinkedHashSet;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import reactor.core.publisher.Mono;

/**
 * This interface defines the contract for a ProcessorMatcher.
 * A ProcessorMatcher is responsible for matching submissions based on certain criteria.
 * The specific criteria for matching are defined by the classes implementing this interface.
 */
public interface ProcessorMatcher {

  /**
   * Checks if the matcher is enabled for a given submission.
   *
   * @param submission A SubmissionInformationDto object.
   * @return A boolean indicating if the matcher is enabled.
   */
  boolean enabled(SubmissionInformationDto submission);

  /**
   * Returns the name of the matcher.
   *
   * @return A string representing the name of the matcher.
   */
  String name();

  /**
   * Returns the field name that the matcher operates on.
   *
   * @return A string representing the field name.
   */
  String fieldName();

  /**
   * Performs the matching operation for a given submission.
   *
   * @param submission A SubmissionInformationDto object.
   * @return A Mono of MatcherResult containing the client numbers of the matches.
   */
  Mono<MatcherResult> matches(SubmissionInformationDto submission);

  /**
   * Returns a BiConsumer that adds a value to a MatcherResult.
   *
   * @return A BiConsumer of MatcherResult and String.
   */
  default BiConsumer<MatcherResult, String> compileResult() {
    return MatcherResult::addValue;
  }

  /**
   * Returns a Supplier that initializes a MatcherResult.
   *
   * @return A Supplier of MatcherResult.
   */
  default Supplier<MatcherResult> initializeResult() {
    return () -> new MatcherResult(fieldName(), new LinkedHashSet<>());
  }
}