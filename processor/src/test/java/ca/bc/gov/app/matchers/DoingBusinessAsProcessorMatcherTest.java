package ca.bc.gov.app.matchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.dto.legacy.ClientDoingBusinessAsDto;
import ca.bc.gov.app.matchers.DoingBusinessAsProcessorMatcher;
import ca.bc.gov.app.matchers.ProcessorMatcher;
import ca.bc.gov.app.service.legacy.LegacyService;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Doing Business As Matcher")
class DoingBusinessAsProcessorMatcherTest {

  private final LegacyService legacyService = mock(LegacyService.class);
  ProcessorMatcher matcher = new DoingBusinessAsProcessorMatcher(legacyService);

  @Test
  @DisplayName("Name matching")
  void shouldMatchName() {
    assertEquals("Doing Business As Fuzzy Matcher", matcher.name());
  }

  @ParameterizedTest
  @MethodSource("legalName")
  @DisplayName("Match or not")
  void shouldMatchOrNot(
      SubmissionInformationDto dto,
      boolean success,
      MatcherResult result,
      Flux<ClientDoingBusinessAsDto> mockData
  ) {

    when(legacyService.matchDba(dto.corporationName()))
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
                new SubmissionInformationDto(1,"James", LocalDate.of(1970, 3, 4), "FM001122334", "Y",
                    "RSP"),
                true,
                null,
                Flux.empty()
            ),
            Arguments.of(
                new SubmissionInformationDto(1,"Marco Polo Navigation Inc", LocalDate.of(1970, 3, 4),
                    "FM001122334", "Y", "RSP"),
                false,
                new MatcherResult("corporationName", String.join(",", "00000000")),
                Flux.just(
                    new ClientDoingBusinessAsDto("00000000", StringUtils.EMPTY, StringUtils.EMPTY,
                        StringUtils.EMPTY, 1L))
            ),
            Arguments.of(
                new SubmissionInformationDto(1,"Lucca", null, null, null, "RSP"),
                false,
                new MatcherResult("corporationName", String.join(",", "00000000", "00000001")),
                Flux.just(
                    new ClientDoingBusinessAsDto("00000000", StringUtils.EMPTY, StringUtils.EMPTY,
                        StringUtils.EMPTY, 1L),
                    new ClientDoingBusinessAsDto("00000001", StringUtils.EMPTY, StringUtils.EMPTY,
                        StringUtils.EMPTY, 1L))
            )
        );
  }

}