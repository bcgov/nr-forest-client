package ca.bc.gov.app.service.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.entity.legacy.ForestClientEntity;
import ca.bc.gov.app.repository.legacy.ForestClientRepository;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Legal Name Matcher")
class LegalNameProcessorMatcherTest {

  private final ForestClientRepository repository = mock(ForestClientRepository.class);
  ProcessorMatcher matcher = new LegalNameProcessorMatcher(repository);

  @Test
  @DisplayName("Name matching")
  void shouldMatchName() {
    assertEquals("Legal Name Matcher", matcher.name());
  }

  @ParameterizedTest
  @MethodSource("legalName")
  @DisplayName("Match or not")
  void shouldMatchOrNot(
      SubmissionInformationDto dto,
      boolean success,
      MatcherResult result,
      Flux<ForestClientEntity> mockData
  ) {

    when(repository.matchBy(dto.legalName()))
        .thenReturn(mockData);

    StepVerifier.FirstStep<MatcherResult> verifier =
        matcher
            .matches(dto)
            .as(StepVerifier::create);

    if (success) {
      verifier.verifyComplete();
    } else {
      verifier
          .expectNext(result)
          .verifyComplete();
    }
  }

  private static Stream<Arguments> legalName() {
    return
        Stream.of(
            Arguments.of(
                new SubmissionInformationDto("James", null, null),
                true,
                null,
                Flux.empty()
            ),
            Arguments.of(
                new SubmissionInformationDto("Marco", null, null),
                false,
                new MatcherResult("legalName", String.join(",", "00000000")),
                Flux.just(new ForestClientEntity().withClientNumber("00000000"))
            ),
            Arguments.of(
                new SubmissionInformationDto("Lucca", null, null),
                false,
                new MatcherResult("legalName", String.join(",", "00000000", "00000001")),
                Flux.just(new ForestClientEntity().withClientNumber("00000000"),
                    new ForestClientEntity().withClientNumber("00000001"))
            )
        );
  }
}
