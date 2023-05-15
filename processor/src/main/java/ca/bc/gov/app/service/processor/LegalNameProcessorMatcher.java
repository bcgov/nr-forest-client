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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class LegalNameProcessorMatcher implements ProcessorMatcher {

  private final ForestClientRepository forestClientRepository;

  @Override
  public String name() {
    return "Legal Name Matcher";
  }

  @Override
  public Mono<MatcherResult> matches(SubmissionInformationDto submission) {

    log.info("{} :: Validating {}",name(),submission.legalName());

    return
        matchBy(submission.legalName())
            .map(ForestClientEntity::getClientNumber)
            .collectList()
            .filter(not(List::isEmpty))
            .map(values ->
                new MatcherResult("legalName", String.join(",", values))
            );
  }

  private Flux<ForestClientEntity> matchBy(String companyName) {
    return
        forestClientRepository
            .matchBy(companyName)
            .doOnNext(entity -> log.info("Found a match {}", entity));

  }

}
