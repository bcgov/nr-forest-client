package ca.bc.gov.app.matchers;

import static java.util.function.Predicate.not;

import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.dto.legacy.ForestClientContactDto;
import ca.bc.gov.app.repository.SubmissionContactRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ContactMatcher implements ProcessorMatcher {

  private final WebClient legacyClientApi;
  private final SubmissionContactRepository contactRepository;

  public ContactMatcher(
      @Qualifier("legacyClientApi") WebClient legacyClientApi,
      SubmissionContactRepository contactRepository
  ) {
    this.legacyClientApi = legacyClientApi;
    this.contactRepository = contactRepository;
  }

  @Override
  public boolean enabled(SubmissionInformationDto submission) {
    return true;
  }

  @Override
  public String name() {
    return "Contact Matcher";
  }

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
                                .path("/api/search/contact")
                                .queryParam("firstName", contact.getFirstName())
                                .queryParam("lastName", contact.getLastName())
                                .queryParam("email", contact.getEmailAddress())
                                .queryParam("phone", contact.getBusinessPhoneNumber())
                                .build()
                    )
                    .exchangeToFlux(response -> response.bodyToFlux(ForestClientContactDto.class))
            )
            .map(ForestClientContactDto::clientNumber)
            .collectList()
            .filter(not(List::isEmpty))
            .map(values ->
                new MatcherResult("contact", String.join(",", values))
            );
  }
}
