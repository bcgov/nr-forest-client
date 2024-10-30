package ca.bc.gov.app.matchers;

import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.dto.submissions.MatcherResult;
import ca.bc.gov.app.dto.submissions.SubmissionInformationDto;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * This class is a component that matches submissions based on the registration number.
 * It implements the ProcessorMatcher interface.
 * It uses a WebClient to interact with the legacy client API.
 */
@Component
@Slf4j
public class RegistrationNumberProcessorMatcher implements ProcessorMatcher {

  private final WebClient legacyClientApi;

  /**
   * This constructor initializes the RegistrationNumberProcessorMatcher with a WebClient for the legacy client API.
   *
   * @param legacyClientApi A WebClient for the legacy client API.
   */
  public RegistrationNumberProcessorMatcher(
      @Qualifier("legacyClientApi") WebClient legacyClientApi
  ) {
    this.legacyClientApi = legacyClientApi;
  }

  /**
   * This method checks if the matcher is enabled for a given submission.
   * It returns true if the registration number of the submission is not blank.
   *
   * @param submission A SubmissionInformationDto object.
   * @return A boolean indicating if the matcher is enabled.
   */
  @Override
  public boolean enabled(SubmissionInformationDto submission) {
    return StringUtils.isNotBlank(submission.registrationNumber());
  }

  /**
   * This method returns the name of the matcher.
   *
   * @return A string representing the name of the matcher.
   */
  @Override
  public String name() {
    return "Registration Number Matcher";
  }

  /**
   * This method returns the field name that the matcher operates on.
   *
   * @return A string representing the field name.
   */
  @Override
  public String fieldName() {
    return "registrationNumber";
  }

  /**
   * This method performs the matching operation for a given submission.
   * It sends a request to the legacy client API to find matches based on the registration number.
   * It then collects the client numbers of the matches into a MatcherResult.
   *
   * @param submission A SubmissionInformationDto object.
   * @return A Mono of MatcherResult containing the client numbers of the matches.
   */
  @Override
  public Mono<MatcherResult> matches(SubmissionInformationDto submission) {

    log.info("{} :: Validating {}", name(), submission.registrationNumber());

    return
        legacyClientApi
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/api/search/registrationOrName")
                        .queryParam("registrationNumber", submission.registrationNumber())
                        .build(Map.of())
            )
            .exchangeToFlux(response -> response.bodyToFlux(ForestClientDto.class))
            .doOnNext(entity -> log.info("Found a match {}", entity))
            .map(ForestClientDto::clientNumber)
            .collect(initializeResult(), compileResult());
  }

}