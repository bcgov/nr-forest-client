package ca.bc.gov.app.service.processor;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Legal Name Matcher")
class LegalNameProcessorMatcherTest {

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension
      .newInstance()
      .options(wireMockConfig().port(10013))
      .configureStaticDsl(true)
      .build();
  ProcessorMatcher matcher = new LegalNameProcessorMatcher(
      WebClient.builder().baseUrl("http://localhost:10013").build()
  );

  @Test
  @DisplayName("Name matching")
  void shouldMatchName() {
    assertEquals("Legal Name Fuzzy Matcher", matcher.name());
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
            get("/api/search/match")
                .willReturn(okJson(mockData))
        );

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
                new SubmissionInformationDto("James", null, null, null, "C"),
                true,
                null,
                "[]"
            ),
            Arguments.of(
                new SubmissionInformationDto("Marco", null, null, null, "C"),
                false,
                new MatcherResult("corporationName", String.join(",", "00000000")),
                "[{\"clientNumber\":\"00000000\"}]"
            ),
            Arguments.of(
                new SubmissionInformationDto("Lucca", null, null, null, "C"),
                false,
                new MatcherResult("corporationName", String.join(",", "00000000", "00000001")),
                "[{\"clientNumber\":\"00000000\"},{\"clientNumber\":\"00000001\"}]"
            )
        );
  }

}