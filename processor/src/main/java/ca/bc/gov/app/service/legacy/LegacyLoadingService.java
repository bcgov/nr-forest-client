package ca.bc.gov.app.service.legacy;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.matchers.ProcessorMatcher;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class LegacyLoadingService {

  private final List<ProcessorMatcher> matchers;

  public Mono<MessagingWrapper<List<MatcherResult>>> matchCheck(
      MessagingWrapper<SubmissionInformationDto> eventMono
  ) {
    return
        validateSubmission(eventMono.payload())
            .map(matchList ->
                new MessagingWrapper<>(
                    matchList,
                    Map.of(
                        ApplicationConstant.SUBMISSION_STATUS, matchList.isEmpty(),
                        ApplicationConstant.SUBMISSION_ID,
                        eventMono.parameters().get(ApplicationConstant.SUBMISSION_ID)
                    )
                )
            );
  }

  private Mono<List<MatcherResult>> validateSubmission(SubmissionInformationDto message) {
    return Flux
        .fromIterable(matchers)
        .filter(matcher -> matcher.enabled(message))
        .doOnNext(matcher -> log.info("Running {}", matcher.name()))
        //If matcher returns empty, all good, if not, it is a problem
        .flatMap(matcher -> matcher.matches(message))
        .doOnNext(results -> log.info("Matched a result {}", results))
        .collectList();
  }

}
