package ca.bc.gov.app.service.processor;

import static java.util.function.Predicate.not;

import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.entity.legacy.ForestClientEntity;
import ca.bc.gov.app.repository.legacy.ForestClientRepository;
import ca.bc.gov.app.util.ProcessorUtil;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class SoleProprietorProcessorMatcher implements ProcessorMatcher {

  private final ForestClientRepository forestClientRepository;

  @Override
  public boolean enabled(SubmissionInformationDto submission) {
    return Arrays.asList("USP", "RSP").contains(submission.clientType());
  }

  @Override
  public String name() {
    return "Sole Proprietor Matcher";
  }

  @Override
  public Mono<MatcherResult> matches(SubmissionInformationDto submission) {

    log.info("{} :: Validating {}", name(), submission.corporationName());

    return
        forestClientRepository
            .findByIndividualNames(
                ProcessorUtil.splitName(submission.corporationName())[1],
                ProcessorUtil.splitName(submission.corporationName())[0]
            )
            .doOnNext(entity -> log.info("Found a match {}", entity))
            .map(ForestClientEntity::getClientNumber)
            .collectList()
            .filter(not(List::isEmpty))
            .map(values ->
                new MatcherResult("corporationName", String.join(",", values))
            );
  }
}
