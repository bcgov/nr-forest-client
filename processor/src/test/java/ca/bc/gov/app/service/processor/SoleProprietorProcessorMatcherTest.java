package ca.bc.gov.app.service.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
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

@DisplayName("Unit Test | Sole Proprietor Matcher")
class SoleProprietorProcessorMatcherTest {

  private final ForestClientRepository repository = mock(ForestClientRepository.class);
  private final ProcessorMatcher matcher = new SoleProprietorProcessorMatcher(repository);


  @Test
  @DisplayName("Name matching")
  void shouldMatchName() {
    assertEquals("Sole Proprietor Matcher", matcher.name());
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

    when(repository.findByIndividualNames(anyString(),anyString()))
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
                new SubmissionInformationDto("James Frank", null, null, null, "USP"),
                true,
                null,
                Flux.empty()
            ),
            Arguments.of(
                new SubmissionInformationDto("Marco Polo", null, null, null, "RSP"),
                false,
                new MatcherResult("corporationName", String.join(",", "00000000")),
                Flux.just(new ForestClientEntity().withClientNumber("00000000"))
            ),
            Arguments.of(
                new SubmissionInformationDto("Lucca DeBiaggio", null, null, null, "USP"),
                false,
                new MatcherResult("corporationName", String.join(",", "00000000", "00000001")),
                Flux.just(new ForestClientEntity().withClientNumber("00000000"),
                    new ForestClientEntity().withClientNumber("00000001"))
            )
        );
  }

}