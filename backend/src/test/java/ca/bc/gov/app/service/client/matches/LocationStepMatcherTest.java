package ca.bc.gov.app.service.client.matches;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.dto.client.MatchResult;
import ca.bc.gov.app.dto.legacy.AddressSearchDto;
import ca.bc.gov.app.dto.legacy.ForestClientDto;
import ca.bc.gov.app.exception.DataMatchException;
import ca.bc.gov.app.service.client.ClientLegacyService;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Location Step Matcher")
@Slf4j
class LocationStepMatcherTest {

  private final ClientLegacyService legacyService = mock(ClientLegacyService.class);
  private final LocationStepMatcher locationStepMatcher = new LocationStepMatcher(
      legacyService);

  @DisplayName("Should match step")
  @ParameterizedTest
  @MethodSource("matchStep")
  void shouldMatchStep(
      ClientSubmissionDto dto,
      Flux<ForestClientDto> emailMatch,
      Flux<ForestClientDto> businessPhoneMatch,
      Flux<ForestClientDto> secondaryPhoneMatch,
      Flux<ForestClientDto> faxMatch,
      Flux<ForestClientDto> addressMatch,
      boolean error,
      boolean fuzzy
  ) {
    when(legacyService.searchGeneric(anyString(), any()))
        .thenReturn(emailMatch, businessPhoneMatch, secondaryPhoneMatch, faxMatch);

    when(legacyService.searchLocation(any(AddressSearchDto.class)))
        .thenReturn(addressMatch);

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
            getAddress(),
            Flux.empty(), //Email
            Flux.empty(), //businessphone
            Flux.empty(), //secondary
            Flux.empty(), //fax
            Flux.just(getForestClientDto("00000001")), //address
            true,
            false
        ),
        Arguments.of(
            getAddress(),
            Flux.empty(), //Email
            Flux.empty(), //businessphone
            Flux.empty(), //secondary
            Flux.empty(), //fax
            Flux.empty(), //address
            false,
            false
        ),
        Arguments.of(
            getAddress(),
            Flux.just(getForestClientDto("00000001")), //Email
            Flux.empty(), //businessphone
            Flux.empty(), //secondary
            Flux.empty(), //fax
            Flux.empty(), //address
            true,
            false
        ),
        Arguments.of(
            getAddress(),
            Flux.empty(), //Email
            Flux.just(getForestClientDto("00000001")), //businessphone
            Flux.empty(), //secondary
            Flux.empty(), //fax
            Flux.empty(), //address
            true,
            false
        ),
        Arguments.of(
            getAddress(),
            Flux.empty(), //Email
            Flux.empty(), //businessphone
            Flux.just(getForestClientDto("00000001")), //secondary
            Flux.empty(), //fax
            Flux.empty(), //address
            true,
            false
        ),
        Arguments.of(
            getAddress(),
            Flux.empty(), //Email
            Flux.empty(), //businessphone
            Flux.empty(), //secondary
            Flux.just(getForestClientDto("00000001")), //fax
            Flux.empty(), //address
            true,
            false
        )
    );
  }

  private static ClientSubmissionDto getAddress() {
    return getDto(
        new ClientAddressDto(
            "1234 Fake St",
            null,
            null,
            new ClientValueTextDto("CA", null),
            new ClientValueTextDto("BC", null),
            "Victoria",
            "V8V4K9",
            "2505551234",
            "2505555678",
            "2505559012",
            "a@a.com",
            null,
            0,
            null
        )
    );
  }

  private static ClientSubmissionDto getDto(ClientAddressDto addressDto) {
    return new ClientSubmissionDto(
        null,
        new ClientLocationDto(
            addressDto == null ? List.of() : List.of(addressDto),
            List.of()
        ),
        null,
        null
    );
  }

  private static ForestClientDto getForestClientDto(String clientNumber) {
    return new ForestClientDto(
        clientNumber,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );
  }

}