package ca.bc.gov.app.matchers;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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


@DisplayName("Unit Test | Registration Number Matcher")
class RegistrationNumberProcessorMatcherTest {

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension
      .newInstance()
      .options(wireMockConfig().port(10011))
      .configureStaticDsl(true)
      .build();

  ProcessorMatcher matcher = new RegistrationNumberProcessorMatcher(
      WebClient.builder().baseUrl("http://localhost:10011").build()
  );

  @Test
  @DisplayName("Name matching")
  void shouldMatchName() {
    assertEquals("Registration Number Matcher", matcher.name());
  }

  @ParameterizedTest
  @MethodSource("registrationNumber")
  @DisplayName("Match or not")
  void shouldBeEnabled(
      SubmissionInformationDto dto,
      boolean success,
      MatcherResult result,
      String mockData
  ) {
    assertTrue(matcher.enabled(dto));
  }

  @ParameterizedTest
  @MethodSource("registrationNumber")
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
            get(urlPathEqualTo("/api/search/registrationOrName"))
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

  private static Stream<Arguments> registrationNumber() {
    return
        Stream.of(
            Arguments.of(
                new SubmissionInformationDto(1,null, null, "00000007", null, "C"),
                true,
                null,
                "[]"
            ),
            Arguments.of(
                new SubmissionInformationDto(1,null, null, "00000006", null, "C"),
                false,
                new MatcherResult("registrationNumber", "00000006"),
                "[{\"clientNumber\":\"00000006\"}]"
            )
        );
  }

}