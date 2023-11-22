package ca.bc.gov.app.service.processor;

import static org.junit.jupiter.api.Assertions.*;
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


@DisplayName("Unit Test | Incorporation Number Matcher")
class IncorporationNumberProcessorMatcherTest {

  private final ForestClientRepository repository = mock(ForestClientRepository.class);
  ProcessorMatcher matcher = new IncorporationNumberProcessorMatcher(repository);

  @Test
  @DisplayName("Name matching")
  void shouldMatchName() {
    assertEquals("Incorporation Number Matcher", matcher.name());
  }

  @ParameterizedTest
  @MethodSource("incorporation")
  @DisplayName("Match or not")
  void shouldBeEnabled(
      SubmissionInformationDto dto,
      boolean success,
      MatcherResult result,
      Flux<ForestClientEntity> mockData
  ) {
    assertTrue(matcher.enabled(dto));
  }

  @ParameterizedTest
  @MethodSource("incorporation")
  @DisplayName("Match or not")
  void shouldMatchOrNot(
      SubmissionInformationDto dto,
      boolean success,
      MatcherResult result,
      Flux<ForestClientEntity> mockData
  ) {

    when(repository.findByIncorporationNumber(dto.incorporationNumber()))
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

  private static Stream<Arguments> incorporation() {
    return
        Stream.of(
            Arguments.of(
                new SubmissionInformationDto(null,null, "00000007", null,"C"),
                true,
                null,
                Flux.empty()
            ),
            Arguments.of(
                new SubmissionInformationDto(null,null, "00000006", null,"C"),
                false,
                new MatcherResult("incorporationNumber", "00000006"),
                Flux.just(new ForestClientEntity().withClientNumber("00000006"))
            )
        );
  }

}