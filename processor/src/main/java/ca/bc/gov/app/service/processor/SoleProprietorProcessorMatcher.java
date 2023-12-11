package ca.bc.gov.app.service.processor;

import static java.util.function.Predicate.not;

import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.util.ProcessorUtil;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class SoleProprietorProcessorMatcher implements ProcessorMatcher {

  private final WebClient legacyClientApi;

  public SoleProprietorProcessorMatcher(
      @Qualifier("legacyClientApi") WebClient legacyClientApi
  ) {
    this.legacyClientApi = legacyClientApi;
  }

  @Override
  public boolean enabled(SubmissionInformationDto submission) {
    return List.of("USP", "RSP").contains(submission.clientType());
  }

  @Override
  public String name() {
    return "Sole Proprietor Matcher";
  }

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
            .doOnNext(entity -> log.info("Found a match {}", entity))
            .map(ForestClientDto::clientNumber)
            .collectList()
            .filter(not(List::isEmpty))
            .map(values ->
                new MatcherResult("corporationName", String.join(",", values))
            );
  }
}
