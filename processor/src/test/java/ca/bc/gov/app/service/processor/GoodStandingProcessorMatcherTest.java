package ca.bc.gov.app.service.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Good Standing Matcher")
class GoodStandingProcessorMatcherTest {

  private final ProcessorMatcher matcher = new GoodStandingProcessorMatcher();

  @Test
  @DisplayName("Name matching")
  void shouldMatchName() {
    assertEquals("Good Standing Matcher", matcher.name());
  }

  @ParameterizedTest
  @MethodSource("goodStanding")
  @DisplayName("Match or not")
  void shouldMatchOrNot(
      SubmissionInformationDto dto,
      boolean success,
      MatcherResult result
  ) {

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

  private static Stream<Arguments> goodStanding() {
    return
        Stream.of(
            Arguments.of(
                new SubmissionInformationDto(null, null, StringUtils.EMPTY),
                false,
                new MatcherResult("goodStanding", "Value not found")
            ),
            Arguments.of(
                new SubmissionInformationDto(null, null, null),
                false,
                new MatcherResult("goodStanding", "Value not found")
            ),
            Arguments.of(
                new SubmissionInformationDto(null, null, "N"),
                false,
                new MatcherResult("goodStanding", "Client not in good standing")
            ),
            Arguments.of(
                new SubmissionInformationDto(null, null, "Y"),
                true,
                null
            )
        );
  }

}