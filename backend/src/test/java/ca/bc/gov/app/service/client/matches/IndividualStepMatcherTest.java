package ca.bc.gov.app.service.client.matches;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

@DisplayName("Unit Test | Individual Step Matcher")
@Slf4j
class IndividualStepMatcherTest {

  private final ClientLegacyService legacyService = mock(ClientLegacyService.class);
  private final IndividualStepMatcher individualStepMatcher = new IndividualStepMatcher(
      legacyService);

  @DisplayName("Should return step matcher enum value for individual steps")
  @Test
  void shouldReturnStepMatcherEnumValueForIndividualSteps() {
    Assertions.assertEquals(StepMatchEnum.STEP1INDIVIDUAL, individualStepMatcher.getStepMatcher());
  }

  @DisplayName("Should match step")
  @ParameterizedTest
  @MethodSource("matchStep")
  void shouldMatchStep(
      ClientSubmissionDto dto,
      Flux<ForestClientDto> individualFuzzyMatch,
      Flux<ForestClientDto> individualFullMatch,
      Flux<ForestClientDto> documentMatch,
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
            .searchIndividual(
                anyString(),
                anyString(),
                any(LocalDate.class),
                anyString()
            )
    )
        .thenReturn(individualFullMatch);

    when(
        legacyService
            .searchDocument(
                anyString(),
                anyString()
            )
    )
        .thenReturn(documentMatch);

    StepVerifier.FirstStep<Void> matcher =
        individualStepMatcher
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
            ClientMatchDataGenerator.getDto(
                "Jhon", 
                "Wick", 
                LocalDate.of(1970, 1, 1), 
                "CDDL", 
                "BC", 
                "1234567",
                null,
                null),
            Flux.empty(),
            Flux.empty(),
            Flux.empty(),
            false,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator.getDto(
                "James", 
                "Wick", 
                LocalDate.of(1970, 1, 1), 
                "CDDL", 
                "AB",
                "7654321", 
                null,
                null),
            Flux.empty(),
            Flux.empty(),
            Flux.just(ClientMatchDataGenerator.getForestClientDto("00000001")),
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator.getDto(
                "Valeria", 
                "Valid", 
                LocalDate.of(1970, 1, 1), 
                "CDDL", 
                "YK",
                "1233210", 
                null,
                null),
            Flux.empty(),
            Flux.just(ClientMatchDataGenerator.getForestClientDto("00000002")),
            Flux.empty(),
            true,
            true
        ),
        Arguments.of(
            ClientMatchDataGenerator.getDto(
                "Papernon", 
                "Pompadour", 
                LocalDate.of(1970, 1, 1), 
                "CDDL", 
                "ON",
                "9994545", 
                null,
                null),
            Flux.just(ClientMatchDataGenerator.getForestClientDto("00000003")),
            Flux.empty(),
            Flux.empty(),
            true,
            true
        ),
        Arguments.of(
            ClientMatchDataGenerator.getDto(
                "Karls", 
                "Enrikvinjon", 
                LocalDate.of(1970, 1, 1), 
                "CDDL", 
                "BC",
                "3337474", 
                null,
                null),
            Flux.just(ClientMatchDataGenerator.getForestClientDto("00000004")),
            Flux.just(ClientMatchDataGenerator.getForestClientDto("00000005")),
            Flux.empty(),
            true,
            true
        ),
        Arguments.of(
            ClientMatchDataGenerator.getDto(
                "Palitz", 
                "Yelvengard", 
                LocalDate.of(1970, 1, 1), 
                "USDL", 
                "AZ",
                "7433374", 
                null,
                null),
            Flux.just(ClientMatchDataGenerator.getForestClientDto("00000006")),
            Flux.just(ClientMatchDataGenerator.getForestClientDto("00000007")),
            Flux.just(ClientMatchDataGenerator.getForestClientDto("00000008")),
            true,
            false
        )
    );
  }

}