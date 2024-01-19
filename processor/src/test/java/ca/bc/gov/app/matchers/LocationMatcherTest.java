package ca.bc.gov.app.matchers;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionLocationDto;
import ca.bc.gov.app.entity.SubmissionLocationEntity;
import ca.bc.gov.app.repository.SubmissionLocationRepository;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Location Matcher")
class LocationMatcherTest {

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension
      .newInstance()
      .options(wireMockConfig().port(10012))
      .configureStaticDsl(true)
      .build();

  private final SubmissionLocationRepository locationRepository = mock(
      SubmissionLocationRepository.class);
  private final ProcessorMatcher matcher = new LocationMatcher(
      WebClient.builder().baseUrl("http://localhost:10012").build(),
      locationRepository
  );

  @Test
  @DisplayName("Name matching")
  void shouldMatchName() {
    assertEquals("Location Matcher", matcher.name());
  }

  @ParameterizedTest
  @MethodSource("location")
  @DisplayName("Match or not")
  void shouldMatchOrNot(
      SubmissionLocationDto dto,
      boolean success,
      MatcherResult result,
      String mockData
  ) {
    wireMockExtension.resetAll();
    wireMockExtension
        .stubFor(
            get(urlPathEqualTo("/api/locations/search"))
                .willReturn(okJson(mockData))
        );

    when(locationRepository.findBySubmissionId(any()))
        .thenReturn(
            Flux.just(
                SubmissionLocationEntity
                    .builder()
                    .submissionId(1)
                    .streetAddress("123 Fake St")
                    .postalCode("A1B2C3")
                    .build()
            )
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
  
  private static Stream<Arguments> location() {
	  return Stream.of(
		        Arguments.of(
		            new SubmissionLocationDto(1, "123 Fake St", "A1B2C3"),
		            true,
		            null,
		            "[]"
		        ),
		        Arguments.of(
		            new SubmissionLocationDto(1, "712 Maria Avenue", "V9B1W7"),
		            false,
		            new MatcherResult("location", String.join(",", "00000000")),
		            "[{\"clientNumber\":\"00000000\"}]"
		        ),
		        Arguments.of(
		            new SubmissionLocationDto(1, "123 Fort St", "12345-1251"),
		            false,
		            new MatcherResult("location", String.join(",", "00000000", "00000001")),
		            "[{\"clientNumber\":\"00000000\"},{\"clientNumber\":\"00000001\"}]"
		        )
		    );
  }

}