package ca.bc.gov.app.matchers;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.entity.SubmissionContactEntity;
import ca.bc.gov.app.repository.SubmissionContactRepository;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Contact Matcher")
class ContactMatcherTest {

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension
      .newInstance()
      .options(wireMockConfig().port(10012))
      .configureStaticDsl(true)
      .build();

  private final SubmissionContactRepository contactRepository = mock(
      SubmissionContactRepository.class);
  private final ProcessorMatcher matcher = new ContactMatcher(
      WebClient.builder().baseUrl("http://localhost:10012").build(),
      contactRepository
  );

  @Test
  @DisplayName("Name matching")
  void shouldMatchName() {
    assertEquals("Contact Matcher", matcher.name());
  }

  @ParameterizedTest
  @MethodSource("contact")
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
            get(urlPathEqualTo("/api/contacts/search"))
                .willReturn(okJson(mockData))
        );

    when(contactRepository.findBySubmissionId(any()))
        .thenReturn(
            Flux.just(
                SubmissionContactEntity
                    .builder()
                    .submissionId(1)
                    .firstName("John")
                    .lastName("Smith")
                    .emailAddress("mail@mail.ca")
                    .businessPhoneNumber("1234567890")
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

  private static Stream<Arguments> contact(){
    return Stream.of(
        Arguments.of(
            new SubmissionInformationDto(1,"James Frank", LocalDate.of(1985, 10, 4), null, "Y",
                "I", null),
            true,
            null,
            "[]"
        ),
        Arguments.of(
            new SubmissionInformationDto(1,"Marco Polo", LocalDate.of(1977, 3, 22), null, "Y",
                "I", null),
            false,
            new MatcherResult("contact", String.join(",", "00000000")),
            "[{\"clientNumber\":\"00000000\"}]"
        ),
        Arguments.of(
            new SubmissionInformationDto(1,"Lucca DeBiaggio", LocalDate.of(1951, 12, 25), null,
                "Y", "I", null),
            false,
            new MatcherResult("contact", String.join(",", "00000000", "00000001")),
            "[{\"clientNumber\":\"00000000\"},{\"clientNumber\":\"00000001\"}]"
        )
    );
  }

}