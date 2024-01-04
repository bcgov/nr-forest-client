package ca.bc.gov.app.matchers;

import static java.util.function.Predicate.not;

import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class IncorporationNumberProcessorMatcher implements ProcessorMatcher {

  private final WebClient legacyClientApi;

  public IncorporationNumberProcessorMatcher(
      @Qualifier("legacyClientApi") WebClient legacyClientApi
  ) {
    this.legacyClientApi = legacyClientApi;
  }

  @Override
  public boolean enabled(SubmissionInformationDto submission) {
    return StringUtils.isNotBlank(submission.incorporationNumber());
  }

  @Override
  public String name() {
    return "Incorporation Number Matcher";
  }

  @Override
  public Mono<MatcherResult> matches(SubmissionInformationDto submission) {

    log.info("{} :: Validating {}", name(), submission.incorporationNumber());

    return
        legacyClientApi
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/api/search/incorporationOrName")
                        .queryParam("incorporationNumber", submission.incorporationNumber())
                        .build(Map.of())
            )
            .exchangeToFlux(response -> response.bodyToFlux(ForestClientDto.class))
            .doOnNext(entity -> log.info("Found a match {}", entity))
            .map(ForestClientDto::clientNumber)
            .collectList()
            .filter(not(List::isEmpty))
            .map(values ->
                new MatcherResult("incorporationNumber", String.join(",", values))
            );
  }

}

