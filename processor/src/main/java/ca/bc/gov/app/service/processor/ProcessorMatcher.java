package ca.bc.gov.app.service.processor;

import ca.bc.gov.app.dto.SubmissionInformationDto;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import reactor.core.publisher.Mono;

public interface ProcessorMatcher {

  String name();
  Function<Mono<SubmissionInformationDto>,Mono<SubmissionInformationDto>> matches();

}
