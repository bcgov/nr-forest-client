package ca.bc.gov.app.matchers;

import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class GoodStandingProcessorMatcher implements ProcessorMatcher {

  @Override
  public boolean enabled(SubmissionInformationDto submission) {
    return true;
  }

  @Override
  public String name() {
    return "Good Standing Matcher";
  }

  @Override
  public Mono<MatcherResult> matches(SubmissionInformationDto submission) {

    log.info("{} :: Validating {}",name(),submission.goodStanding());

    if (StringUtils.isBlank(submission.goodStanding())) {
      return Mono.just(new MatcherResult("goodStanding", "Value not found"));
    }

    if (StringUtils.equalsIgnoreCase("N", submission.goodStanding())) {
      return Mono.just(new MatcherResult("goodStanding", "Client not in good standing"));
    }

    return Mono.empty();
  }
}
