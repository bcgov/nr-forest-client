package ca.bc.gov.app.matchers;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okForContentType;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Sole Proprietor Matcher")
class SoleProprietorProcessorMatcherTest {

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension
      .newInstance()
      .options(wireMockConfig().port(10010))
      .configureStaticDsl(true)
      .build();

  private final ProcessorMatcher matcher = new SoleProprietorProcessorMatcher(
      WebClient.builder().baseUrl("http://localhost:10010").build()
  );

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
      String mockData
  ) {

    wireMockExtension.resetAll();
    wireMockExtension
        .stubFor(
            get(urlPathEqualTo("/api/search/individual"))
                .willReturn(
                    okForContentType("application/json", mockData)
                        .withHeader("Content-Type", "application/json")
                )
        );

    matcher
        .matches(dto)
        .as(StepVerifier::create)
        .expectNext(result)
        .verifyComplete();

  }

  private static Stream<Arguments> legalName() {
    return
        Stream.of(
            Arguments.of(
                new SubmissionInformationDto(1, "James Frank", LocalDate.of(2023, 4, 5), null, null,
                    "USP"),
                true,
                new MatcherResult("corporationName", Set.of()),
                "[]"
            ),
            Arguments.of(
                new SubmissionInformationDto(1, "Marco Polo", LocalDate.of(2023, 9, 12), null, null,
                    "RSP"),
                false,
                new MatcherResult("corporationName", Set.of("00000000")),
                "[{\"clientNumber\":\"00000000\"}]"
            ),
            Arguments.of(
                new SubmissionInformationDto(1, "Lucca DeBiaggio", LocalDate.of(2023, 10, 11), null,
                    null, "USP"),
                false,
                new MatcherResult("corporationName", Set.of("00000000", "00000001")),
                "[{\"clientNumber\":\"00000000\"},{\"clientNumber\":\"00000001\"}]"
            )
        );
  }

}