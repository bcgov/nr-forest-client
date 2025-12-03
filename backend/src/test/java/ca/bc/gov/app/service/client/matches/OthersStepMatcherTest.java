package ca.bc.gov.app.service.client.matches;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.MatchResult;
import ca.bc.gov.app.dto.client.StepMatchEnum;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.exception.DataMatchException;
import ca.bc.gov.app.extensions.ClientMatchDataGenerator;
import ca.bc.gov.app.service.client.ClientLegacyService;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Condition;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Other types Step Matcher")
@Slf4j
class OthersStepMatcherTest {

  private final ClientLegacyService legacyService = mock(ClientLegacyService.class);
  private final OthersStepMatcher stepMatcher = new OthersStepMatcher(legacyService);

  @DisplayName("Should return step matcher enum value for other steps")
  @Test
  void shouldReturnStepMatcherEnumValueForIndividualSteps() {
    Assertions.assertEquals(StepMatchEnum.STEP1OTHERS, stepMatcher.getStepMatcher());
  }

  @DisplayName("Should match step")
  @ParameterizedTest(name = "error {4} fuzzy {5} when provided with {1}, {2}, {3}")
  @MethodSource("matchStep")
  void shouldMatchStep(
      ClientSubmissionDto dto,
      Flux<ForestClientDto> clientNameFuzzyMatch,
      Flux<ForestClientDto> fullNameMatch,
      Flux<ForestClientDto> acronymMatch,
      boolean error,
      boolean fuzzy
  ) {

    when(legacyService.searchGeneric(anyString(), anyString()))
        .thenReturn(fullNameMatch, acronymMatch);

    when(legacyService.searchGeneric(anyString(), anyString(), anyString()))
        .thenReturn(clientNameFuzzyMatch);

    StepVerifier.FirstStep<Void> matcher =
        stepMatcher
            .matchStep(dto)
            .as(StepVerifier::create);

    if (error) {
      matcher
          .consumeErrorWith(errorContent ->
              assertThat(errorContent)
                  .isInstanceOf(DataMatchException.class)
                  .hasMessage("409 CONFLICT \"Match found on existing data.\"")
                  .extracting("matches")
                  .isInstanceOf(List.class)
                  .asInstanceOf(InstanceOfAssertFactories.list(Object.class))
                  .has(
                      new Condition<>(
                          matchResult ->
                              matchResult
                                  .stream()
                                  .map(m -> (MatchResult) m)
                                  .anyMatch(m -> m.fuzzy() == fuzzy),
                          "MatchResult with fuzzy value %s",
                          fuzzy
                      )
                  )

          )
          .verify();
    } else {
      matcher.verifyComplete();
    }


  }

  private static Stream<Arguments> matchStep() {
    return Stream.of(
        Arguments.of(
            ClientMatchDataGenerator
                .getOther(
                    "Government Agency",
                    null,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "G"
                ),
            named("no fuzzy name", Flux.empty()),
            named("no full name", Flux.empty()),
            named("no acronym", Flux.empty()),
            false,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getOther(
                    "Government Agency",
                    null,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "G"
                ),
            named("fuzzy name matched",
                Flux.just(ClientMatchDataGenerator.getForestClientDto("00000001"))),
            named("no full name", Flux.empty()),
            named("no acronym", Flux.empty()),
            true,
            true
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getOther(
                    "Government Agency",
                    null,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "G"
                ),
            named("no fuzzy name", Flux.empty()),
            named("full name matched",
                Flux.just(ClientMatchDataGenerator.getForestClientDto("00000002"))),
            named("no acronym", Flux.empty()),
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getOther(
                    "Government Agency",
                    null,
                    StringUtils.EMPTY,
                    "ABC",
                    "G"
                ),
            named("no fuzzy name", Flux.empty()),
            named("no full name", Flux.empty()),
            named("acronym matched",
                Flux.just(ClientMatchDataGenerator.getForestClientDto("00000003"))),
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getOther(
                    "Government Agency",
                    null,
                    StringUtils.EMPTY,
                    "ABC",
                    "G"
                ),
            named("fuzzy name matched",
                Flux.just(ClientMatchDataGenerator.getForestClientDto("00000001"))),
            named("full name matched",
                Flux.just(ClientMatchDataGenerator.getForestClientDto("00000002"))),
            named("acronym matched",
                Flux.just(ClientMatchDataGenerator.getForestClientDto("00000003"))),
            true,
            false
        )
    );
  }

}