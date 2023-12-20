package ca.bc.gov.app.service.processor;

import static java.util.function.Predicate.not;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.dto.legacy.ClientDoingBusinessAsDto;
import ca.bc.gov.app.service.legacy.LegacyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class DoingBusinessAsProcessorMatcher implements ProcessorMatcher {

  private final LegacyService legacyService;

  @Override
  public boolean enabled(SubmissionInformationDto submission) {
    return "RSP".equalsIgnoreCase(submission.clientType());
  }

  @Override
  public String name() {
    return "Doing Business As Fuzzy Matcher";
  }

  @Override
  public Mono<MatcherResult> matches(SubmissionInformationDto submission) {

    log.info("{} :: Validating {}", name(), submission.corporationName());

    return
        matchBy(submission.corporationName())
            .map(ClientDoingBusinessAsDto::clientNumber)
            .collectList()
            .filter(not(List::isEmpty))
            .map(values ->
                new MatcherResult(ApplicationConstant.MATCH_PARAM_NAME, String.join(",", values))
            );
  }

  private Flux<ClientDoingBusinessAsDto> matchBy(String companyName) {
    return
        legacyService
            .matchDba(companyName)
            .doOnNext(entity -> log.info("Found a match {}", entity));

  }

}
