package ca.bc.gov.app.service.processor;

import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import reactor.core.publisher.Mono;

public interface ProcessorMatcher {

  String name();
  Mono<MatcherResult> matches(SubmissionInformationDto submission);
}
