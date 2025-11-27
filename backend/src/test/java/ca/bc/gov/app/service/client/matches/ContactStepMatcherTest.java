package ca.bc.gov.app.service.client.matches;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.MatchResult;
import ca.bc.gov.app.dto.legacy.ContactSearchDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.exception.DataMatchException;
import ca.bc.gov.app.extensions.ClientMatchDataGenerator;
import ca.bc.gov.app.service.client.ClientLegacyService;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Condition;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Contact Step Matcher")
@Slf4j
class ContactStepMatcherTest {

  private final ClientLegacyService legacyService = mock(ClientLegacyService.class);
  private final ContactStepMatcher locationStepMatcher = new ContactStepMatcher(
      legacyService);

  @DisplayName("Should match step")
  @ParameterizedTest
  @MethodSource("matchStep")
  @SuppressWarnings("unchecked")
  void shouldMatchStep(
      ClientSubmissionDto dto,
      Flux<ForestClientDto> emailMatch,
      Flux<ForestClientDto> businessPhoneMatch,
      Flux<ForestClientDto> secondaryPhoneMatch,
      Flux<ForestClientDto> faxMatch,
      Flux<ForestClientDto> contactMatch,
      boolean error,
      boolean fuzzy
  ) {
    when(legacyService.searchGeneric(anyString(), anyString()))
        .thenReturn(emailMatch, businessPhoneMatch, secondaryPhoneMatch, faxMatch);

    when(legacyService.searchContact(any(ContactSearchDto.class)))
        .thenReturn(contactMatch);

    StepVerifier.FirstStep<Void> matcher =
        locationStepMatcher
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
                  .asInstanceOf(InstanceOfAssertFactories.list(MatchResult.class))
                  .has(
                      new Condition<>(
                          matchResult ->
                              matchResult
                                  .stream()
                                  .map(m -> (MatchResult) m)
                                  .anyMatch(MatchResult::fuzzy),
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
            ClientMatchDataGenerator.getContact(),
            Flux.empty(), //Email
            Flux.empty(), //businessphone
            Flux.empty(), //secondary
            Flux.empty(), //fax
            Flux.just(ClientMatchDataGenerator.getForestClientDto("00000001")), //contact
            true,
            true
        ),
        Arguments.of(
            ClientMatchDataGenerator.getContact(),
            Flux.empty(), //Email
            Flux.empty(), //businessphone
            Flux.empty(), //secondary
            Flux.empty(), //fax
            Flux.empty(), //contact
            false,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator.getContact(),
            Flux.just(ClientMatchDataGenerator.getForestClientDto("00000001")), //Email
            Flux.empty(), //businessphone
            Flux.empty(), //secondary
            Flux.empty(), //fax
            Flux.empty(), //contact
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator.getContact(),
            Flux.empty(), //Email
            Flux.just(ClientMatchDataGenerator.getForestClientDto("00000001")), //businessphone
            Flux.empty(), //secondary
            Flux.empty(), //fax
            Flux.empty(), //contact
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator.getContact(),
            Flux.empty(), //Email
            Flux.empty(), //businessphone
            Flux.just(ClientMatchDataGenerator.getForestClientDto("00000001")), //secondary
            Flux.empty(), //fax
            Flux.empty(), //contact
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator.getContact(),
            Flux.empty(), //Email
            Flux.empty(), //businessphone
            Flux.empty(), //secondary
            Flux.just(ClientMatchDataGenerator.getForestClientDto("00000001")), //fax
            Flux.empty(), //contact
            true,
            false
        )
    );
  }


}