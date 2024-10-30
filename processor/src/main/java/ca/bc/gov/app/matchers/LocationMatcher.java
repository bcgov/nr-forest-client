package ca.bc.gov.app.matchers;

import ca.bc.gov.app.dto.legacy.ForestClientLocationDto;
import ca.bc.gov.app.dto.submissions.MatcherResult;
import ca.bc.gov.app.dto.submissions.SubmissionInformationDto;
import ca.bc.gov.app.repository.SubmissionLocationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * This class is a component that matches submissions based on location.
 * It implements the ProcessorMatcher interface.
 * It uses a WebClient to interact with the legacy client API and a SubmissionLocationRepository to interact with the submission location data.
 */
@Component
@Slf4j
public class LocationMatcher implements ProcessorMatcher {

  private final WebClient legacyClientApi;
  private final SubmissionLocationRepository locationRepository;

  /**
   * This constructor initializes the LocationMatcher with a WebClient for the legacy client API and a SubmissionLocationRepository.
   *
   * @param legacyClientApi A WebClient for the legacy client API.
   * @param locationRepository A SubmissionLocationRepository for the submission location data.
   */
  public LocationMatcher(
      @Qualifier("legacyClientApi") WebClient legacyClientApi,
      SubmissionLocationRepository locationRepository) {
    super();
    this.legacyClientApi = legacyClientApi;
    this.locationRepository = locationRepository;
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
    return "Location Matcher";
  }

  /**
   * This method returns the field name that the matcher operates on.
   *
   * @return A string representing the field name.
   */
  @Override
  public String fieldName() {
    return "location";
  }

  /**
   * This method performs the matching operation for a given submission.
   * It retrieves the location for the submission from the SubmissionLocationRepository and sends a request to the legacy client API to find matches based on the address and postal code.
   * It then collects the client numbers of the matches into a MatcherResult.
   *
   * @param submission A SubmissionInformationDto object.
   * @return A Mono of MatcherResult containing the client numbers of the matches.
   */
  @Override
  public Mono<MatcherResult> matches(SubmissionInformationDto submission) {

    log.info("{} :: Validating {}", name(), submission.corporationName());

    return locationRepository
        .findBySubmissionId(submission.submissionId())
        .flatMap(location -> legacyClientApi
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/api/locations/search")
                        .queryParam("address", location.getStreetAddress())
                        .queryParam("postalCode", location.getPostalCode())
                        .build()
            )
            .exchangeToFlux(response -> response.bodyToFlux(ForestClientLocationDto.class))
        )
        .map(ForestClientLocationDto::clientNumber)
        .collect(initializeResult(), compileResult());
  }

}