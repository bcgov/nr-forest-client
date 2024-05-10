package ca.bc.gov.app.matchers;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * This class is a component that matches submissions based on the legal name.
 * It implements the ProcessorMatcher interface.
 * It uses a WebClient to interact with the legacy client API.
 */
@Component
@Slf4j
public class LegalNameProcessorMatcher implements ProcessorMatcher {

  private final WebClient legacyClientApi;

  /**
   * This constructor initializes the LegalNameProcessorMatcher with a WebClient for the legacy client API.
   *
   * @param legacyClientApi A WebClient for the legacy client API.
   */
  public LegalNameProcessorMatcher(
      @Qualifier("legacyClientApi") WebClient legacyClientApi
  ) {
    this.legacyClientApi = legacyClientApi;
  }

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
    return "Legal Name Fuzzy Matcher";
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
   * It sends a request to the legacy client API to find matches based on the corporation name.
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
                        .path("/api/search/match")
                        .queryParam("companyName", submission.corporationName())
                        .build(Map.of())
            )
            .exchangeToFlux(response -> response.bodyToFlux(ForestClientDto.class))
            .map(ForestClientDto::clientNumber)
            .doOnNext(entity -> log.info("Found a match {}", entity))
            .collect(initializeResult(), compileResult());
  }

}