package ca.bc.gov.app.service.processor;

import static java.util.function.Predicate.not;

import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.entity.legacy.ForestClientEntity;
import ca.bc.gov.app.repository.legacy.ForestClientRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class IncorporationNumberProcessorMatcher implements ProcessorMatcher {
  private final ForestClientRepository forestClientRepository;

  @Override
  public String name() {
    return "Incorporation Number Matcher";
  }

  @Override
  public Mono<MatcherResult> matches(SubmissionInformationDto submission) {

    log.info("{} :: Validating {}", name(), submission.incorporationNumber());

    return
        forestClientRepository
            .findByIncorporationNumber(submission.incorporationNumber())
            .doOnNext(entity -> log.info("Found a match {}", entity))
            .map(ForestClientEntity::getClientNumber)
            .collectList()
            .filter(not(List::isEmpty))
            .map(values ->
                new MatcherResult("incorporationNumber", String.join(",", values))
            );
  }

}

