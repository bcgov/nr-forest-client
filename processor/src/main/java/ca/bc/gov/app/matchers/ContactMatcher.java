package ca.bc.gov.app.matchers;

import ca.bc.gov.app.dto.legacy.ForestClientContactDto;
import ca.bc.gov.app.dto.submissions.MatcherResult;
import ca.bc.gov.app.dto.submissions.SubmissionInformationDto;
import ca.bc.gov.app.repository.SubmissionContactRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * This class is a component that matches contacts. It implements the ProcessorMatcher interface. It
 * uses a WebClient to interact with the legacy client API and a SubmissionContactRepository to
 * retrieve submission contacts.
 */
@Component
@Slf4j
public class ContactMatcher implements ProcessorMatcher {

  private final WebClient legacyClientApi;
  private final SubmissionContactRepository contactRepository;

  /**
   * This constructor initializes the ContactMatcher with a WebClient for the legacy client API and
   * a SubmissionContactRepository.
   *
   * @param legacyClientApi   A WebClient for the legacy client API.
   * @param contactRepository A SubmissionContactRepository to retrieve submission contacts.
   */
  public ContactMatcher(
      @Qualifier("legacyClientApi") WebClient legacyClientApi,
      SubmissionContactRepository contactRepository
  ) {
    this.legacyClientApi = legacyClientApi;
    this.contactRepository = contactRepository;
  }

  /**
   * This method checks if the matcher is enabled for a given submission. It always returns true.
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
    return "Contact Matcher";
  }

  /**
   * This method returns the field name that the matcher operates on.
   *
   * @return A string representing the field name.
   */
  @Override
  public String fieldName() {
    return "contact";
  }

  /**
   * This method performs the matching operation for a given submission. It retrieves the contact
   * for the submission from the contact repository and sends a request to the legacy client API to
   * find matches. It then collects the client numbers of the matches into a MatcherResult.
   *
   * @param submission A SubmissionInformationDto object.
   * @return A Mono of MatcherResult containing the client numbers of the matches.
   */
  @Override
  public Mono<MatcherResult> matches(SubmissionInformationDto submission) {

    log.info("{} :: Validating {}", name(), submission.corporationName());

    return
        contactRepository
            .findBySubmissionId(submission.submissionId())
            .flatMap(contact ->
                legacyClientApi
                    .get()
                    .uri(
                        uriBuilder ->
                            uriBuilder
                                .path("/api/contacts/search")
                                .queryParam("firstName", contact.getFirstName())
                                .queryParam("lastName", contact.getLastName())
                                .queryParam("email", contact.getEmailAddress())
                                .queryParam("phone", contact.getBusinessPhoneNumber())
                                .build()
                    )
                    .exchangeToFlux(response -> response.bodyToFlux(ForestClientContactDto.class))
            )
            .map(ForestClientContactDto::clientNumber)
            .collect(initializeResult(), compileResult());
  }
}
