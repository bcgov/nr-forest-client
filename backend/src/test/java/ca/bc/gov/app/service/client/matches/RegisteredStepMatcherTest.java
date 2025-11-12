package ca.bc.gov.app.service.client.matches;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.MatchResult;
import ca.bc.gov.app.dto.client.StepMatchEnum;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.exception.DataMatchException;
import ca.bc.gov.app.extensions.ClientMatchDataGenerator;
import ca.bc.gov.app.service.client.ClientLegacyService;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SuppressWarnings("unchecked")
@DisplayName("Unit Test | Registered Step Matcher")
@Slf4j
class RegisteredStepMatcherTest {

  private final ClientLegacyService legacyService = mock(ClientLegacyService.class);
  private final RegisteredStepMatcher registeredStepMatcher = new RegisteredStepMatcher(
      legacyService);

  @DisplayName("Should return step matcher enum value for individual steps")
  @Test
  void shouldReturnStepMatcherEnumValueForIndividualSteps() {
    Assertions.assertEquals(StepMatchEnum.STEP1REGISTERED, registeredStepMatcher.getStepMatcher());
  }

  @DisplayName("Should match step")
  @ParameterizedTest(name = "error {8} fuzzy {9} when provided with {1}, {2}, {3}, {4}, {5}, {6}, {7}")
  @MethodSource("matchStep")
  void shouldMatchStep(
      ClientSubmissionDto dto,
      Flux<ForestClientDto> individualFuzzyMatch,
      Flux<ForestClientDto> clientNameFuzzyMatch,
      Flux<ForestClientDto> clientRegistrationFullMatch,
      Flux<ForestClientDto> fullNameMatch,
      Flux<ForestClientDto> acronymMatch,
      Flux<ForestClientDto> dbaFuzzyMatch,
      Flux<ForestClientDto> dbaFullMatch,
      boolean error,
      boolean fuzzy
  ) {
    when(
        legacyService
            .searchIndividual(
                anyString(),
                anyString(),
                any(LocalDate.class),
                eq(null)
            )
    )
        .thenReturn(individualFuzzyMatch);

    when(
        legacyService
            .searchLegacy(
                anyString(),
                eq(null),
                eq(null),
                eq(null)
            )
    )
        .thenReturn(clientRegistrationFullMatch);

    when(legacyService.searchGeneric(anyString(), anyString()))
        .thenReturn(fullNameMatch, acronymMatch);

    when(legacyService.searchGeneric(anyString(), anyString(), anyString()))
        .thenReturn(clientNameFuzzyMatch, dbaFuzzyMatch);

    when(legacyService.searchGeneric(anyString(), anyMap()))
        .thenReturn(dbaFullMatch);

    StepVerifier.FirstStep<Void> matcher =
        registeredStepMatcher
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
                  .asList()
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
                .getRegistered(
                    "C123456",
                    "Fake Corp",
                    "C",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "C"
                ),
            named("no individual", Flux.empty()),
            named("no fuzzy name", Flux.empty()),
            named("no registration", Flux.empty()),
            named("no full name", Flux.empty()),
            named("no acronym", Flux.empty()),
            named("no dba fuzzy", Flux.empty()),
            named("no dba full", Flux.empty()),
            false,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getRegisteredSP(
                    "C123456",
                    "Fake Corp",
                    StringUtils.EMPTY,
                    "Fake Corp",
                    StringUtils.EMPTY,
                    "Johnathan",
                    "Wick",
                    LocalDate.of(1970, 1, 12)
                ),
            named("individual matched", Flux.just(ClientMatchDataGenerator.getForestClientDto("00000001"))),
            named("no fuzzy name", Flux.empty()),
            named("no registration", Flux.empty()),
            named("no full name", Flux.empty()),
            named("no acronym", Flux.empty()),
            named("no dba fuzzy", Flux.empty()),
            named("no dba full", Flux.empty()),
            true,
            true
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getRegistered(
                    "C123456",
                    "Fake Corp",
                    "C",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "C"
                ),
            named("no individual", Flux.empty()),
            named("no fuzzy name", Flux.empty()),
            named("registration matched", Flux.just(ClientMatchDataGenerator.getForestClientDto("00000001"))),
            named("no full name", Flux.empty()),
            named("no acronym", Flux.empty()),
            named("no dba fuzzy", Flux.empty()),
            named("no dba full", Flux.empty()),
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getRegistered(
                    "C123456",
                    "Fake Corp",
                    "C",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "C"
                ),
            named("no individual", Flux.empty()),
            named("no fuzzy name", Flux.empty()),
            named("no registration", Flux.empty()),
            named("no full name", Flux.empty()),
            named("acronym matched",Flux.just(ClientMatchDataGenerator.getForestClientDto("00000001"))),
            named("no dba fuzzy", Flux.empty()),
            named("no dba full", Flux.empty()),
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getRegistered(
                    "C123456",
                    "Fake Corp",
                    "C",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "C"
                ),
            named("no individual", Flux.empty()),
            named("fuzzy name matched", Flux.just(ClientMatchDataGenerator.getForestClientDto("00000001"))),
            named("no registration", Flux.empty()),
            named("no full name", Flux.empty()),
            named("no acronym", Flux.empty()),
            named("no dba fuzzy", Flux.empty()),
            named("no dba full", Flux.empty()),
            true,
            true
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getRegistered(
                    "C123456",
                    "Fake Corp",
                    "C",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "C"
                ),
            named("no individual", Flux.empty()),
            named("fuzzy name matched", Flux.just(ClientMatchDataGenerator.getForestClientDto("00000001"))),
            named("no registration", Flux.empty()),
            named("full name matched", Flux.just(ClientMatchDataGenerator.getForestClientDto("00000002"))),
            named("no acronym", Flux.empty()),
            named("no dba fuzzy", Flux.empty()),
            named("no dba full", Flux.empty()),
            true,
            true
        ),


        Arguments.of(
            ClientMatchDataGenerator
                .getRegistered(
                    "C123456",
                    "Fake Corp",
                    "C",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "C"
                ),
            named("no individual", Flux.empty()),
            named("no fuzzy name", Flux.empty()),
            named("no registration", Flux.empty()),
            named("no full name", Flux.empty()),
            named("no acronym", Flux.empty()),
            named("no dba fuzzy", Flux.just(ClientMatchDataGenerator.getForestClientDto("00000001"))),
            named("no dba full", Flux.empty()),
            true,
            true
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getRegistered(
                    "C123456",
                    "Fake Corp",
                    "C",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "C"
                ),
            named("no individual", Flux.empty()),
            named("no fuzzy name", Flux.empty()),
            named("no registration", Flux.empty()),
            named("no full name", Flux.empty()),
            named("no acronym", Flux.empty()),
            named("dba fuzzy matched", Flux.just(ClientMatchDataGenerator.getForestClientDto("00000001"))),
            named("dba full matched", Flux.just(ClientMatchDataGenerator.getForestClientDto("00000001"))),
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getRegistered(
                    "C123456",
                    "Fake Corp",
                    "C",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "C"
                ),
            named("no individual", Flux.empty()),
            named("fuzzy name matched", Flux.just(ClientMatchDataGenerator.getForestClientDto("00000001"))),
            named("no registration", Flux.empty()),
            named("full name matched", Flux.just(ClientMatchDataGenerator.getForestClientDto("00000002"))),
            named("no acronym", Flux.empty()),
            named("dba fuzzy matched", Flux.just(ClientMatchDataGenerator.getForestClientDto("00000003"))),
            named("dba full matched", Flux.just(ClientMatchDataGenerator.getForestClientDto("00000004"))),
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator
            .getRegisteredSP(
                "C123456",
                "Fake Corp",
                StringUtils.EMPTY,
                "Fake Corp",
                StringUtils.EMPTY,
                "Johnathan",
                "Wick",
                LocalDate.of(1970, 1, 12)
            ),
            named("individual matched", Flux.just(ClientMatchDataGenerator.getForestClientDto("00000001"))),
            named("fuzzy name matched", Flux.just(ClientMatchDataGenerator.getForestClientDto("00000002"))),
            named("registration matched", Flux.just(ClientMatchDataGenerator.getForestClientDto("00000003"))),
            named("full name matched", Flux.just(ClientMatchDataGenerator.getForestClientDto("00000004"))),
            named("acronym matched",Flux.just(ClientMatchDataGenerator.getForestClientDto("00000005"))),
            named("dba fuzzy matched", Flux.just(ClientMatchDataGenerator.getForestClientDto("00000006"))),
            named("dba full matched", Flux.just(ClientMatchDataGenerator.getForestClientDto("00000007"))),
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getRegisteredSP(
                    "FM123456",
                    "Santa Marta Corp",
                    StringUtils.EMPTY,
                    "Santa Marta Corp",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    LocalDate.of(1970, 1, 12)
                ),
            named("no individual request done", Flux.empty()),
            named("no fuzzy name", Flux.empty()),
            named("no registration", Flux.empty()),
            named("no full name", Flux.empty()),
            named("no acronym", Flux.empty()),
            named("no dba fuzzy", Flux.empty()),
            named("no dba full", Flux.empty()),
            false,
            false
        )
    );
  }

}