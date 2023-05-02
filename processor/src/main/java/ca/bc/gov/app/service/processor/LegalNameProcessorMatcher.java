package ca.bc.gov.app.service.processor;

import static java.util.function.Predicate.not;

import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.entity.legacy.ForestClientEntity;
import ca.bc.gov.app.exception.MatchFoundException;
import ca.bc.gov.app.repository.legacy.ForestClientRepository;
import java.util.List;
import java.util.function.Function;
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
  public Function<Mono<SubmissionInformationDto>, Mono<SubmissionInformationDto>> matches() {
    return submissionMono ->
        submissionMono
            .map(SubmissionInformationDto::legalName)
            .flatMap(legalName ->
                matchBy(legalName)
                    .doOnNext(x -> log.info("1 - Matched {} with {}",legalName,x))
                    .map(ForestClientEntity::getClientNumber)
                    .doOnNext(x -> log.info("2 - Matched {} with {}",legalName,x))
                    .collectList()
                    .filter(not(List::isEmpty))
                    .doOnNext(x -> log.info("3 - Matched {} with {}",legalName,x))
                    .flatMap(values -> Mono.error(
                            new MatchFoundException(
                                "legalName",
                                String.join(",", values)
                            )
                        )
                    )
            )
            .switchIfEmpty(submissionMono)
            .flatMap(matches -> submissionMono);
  }

  private Flux<ForestClientEntity> matchBy(String companyName) {

    return
        forestClientRepository
            .matchBy(companyName)
            .doOnNext(entity -> log.info("Found a match {}", entity));

  }

}
