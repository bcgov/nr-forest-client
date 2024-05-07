package ca.bc.gov.app.matchers;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.util.ProcessorUtil;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * This class is a component that matches submissions based on individual client type.
 * It implements the ProcessorMatcher interface.
 * It uses a WebClient to interact with the legacy client API.
 */
@Component
@Slf4j
public class IndividualProcessorMatcher implements ProcessorMatcher {

  private final WebClient legacyClientApi;

  /**
   * This constructor initializes the IndividualProcessorMatcher with a WebClient for the legacy client API.
   *
   * @param legacyClientApi A WebClient for the legacy client API.
   */
  public IndividualProcessorMatcher(
      @Qualifier("legacyClientApi") WebClient legacyClientApi
  ) {
    this.legacyClientApi = legacyClientApi;
  }

  /**
   * This method checks if the matcher is enabled for a given submission.
   * It returns true if the client type of the submission is "I".
   *
   * @param submission A SubmissionInformationDto object.
   * @return A boolean indicating if the matcher is enabled.
   */
  @Override
  public boolean enabled(SubmissionInformationDto submission) {
    return "I".equalsIgnoreCase(submission.clientType());
  }

  /**
   * This method returns the name of the matcher.
   *
   * @return A string representing the name of the matcher.
   */
  @Override
  public String name() {
    return "Individual Matcher";
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
   * It sends a request to the legacy client API to find matches based on the first name, last name, and date of birth.
   * It then collects the client numbers of the matches into a MatcherResult.
   *
   * @param submission A SubmissionInformationDto object.
   * @return A Mono of MatcherResult containing the client numbers of the matches.
   */
  @Override
  public Mono<MatcherResult> matches(SubmissionInformationDto submission) {

    log.info("{} :: Validating {}", name(), submission.corporationName());

    return
        legacyClientApi
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/api/search/individual")
                        .queryParam("firstName",
                            ProcessorUtil.splitName(submission.corporationName())[1])
                        .queryParam("lastName",
                            ProcessorUtil.splitName(submission.corporationName())[0])
                        .queryParam("dob", submission.dateOfBirth())
                        .build(Map.of())
            )
            .exchangeToFlux(response -> response.bodyToFlux(ForestClientDto.class))
            .map(ForestClientDto::clientNumber)
            .collect(initializeResult(), compileResult());
  }

}