package ca.bc.gov.app.controller.client;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.MatchResult;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.ClientMatchDataGenerator;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import reactor.core.publisher.Mono;

@DisplayName("Integrated Test | Client Match for Location Controller")
class ClientMatchLocationControllerIntegrationTest extends AbstractTestContainerIntegrationTest {

  @RegisterExtension
  static WireMockExtension legacyStub = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10060)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();

  @BeforeEach
  public void reset() {
    client = client
        .mutateWith(csrf())
        .mutateWith(mockUser().roles(ApplicationConstant.ROLE_EDITOR))
        .mutateWith(
            mockJwt()
                .jwt(jwt -> jwt.claims(claims -> claims.putAll(TestConstants.getClaims("idir"))))
                .authorities(new SimpleGrantedAuthority("ROLE_" + ApplicationConstant.ROLE_EDITOR))
        )
        .mutate()
        .responseTimeout(Duration.ofSeconds(10))
        .build();
  }

  @ParameterizedTest
  @MethodSource("individualMatch")
  @DisplayName("List and Search")
  void shouldRunMatch(
      ClientSubmissionDto dto,
      String emailMatch,
      String businessPhoneMatch,
      String secondaryPhoneMatch,
      String faxMatch,
      String addressMatch,
      boolean error,
      boolean fuzzy
  ) {
    legacyStub.resetAll();

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/email"))
                .withQueryParam("email", equalTo("a@a.com"))
                .willReturn(okJson(emailMatch))
        );

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/phone"))
                .withQueryParam("phone", equalTo("2505551234"))
                .willReturn(okJson(businessPhoneMatch))
        );

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/phone"))
                .withQueryParam("phone", equalTo("2505555678"))
                .willReturn(okJson(secondaryPhoneMatch))
        );

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/phone"))
                .withQueryParam("phone", equalTo("2505559012"))
                .willReturn(okJson(faxMatch))
        );

    legacyStub
        .stubFor(
            post(urlPathEqualTo("/api/search/address"))
                .willReturn(okJson(addressMatch))
        );

    ResponseSpec response =
        client
            .post()
            .uri("/api/clients/matches")
            .header("X-STEP", "2")
            .body(Mono.just(dto), ClientSubmissionDto.class)
            .exchange();

    if (error) {
      response
          .expectStatus()
          .isEqualTo(HttpStatus.CONFLICT)
          .expectBodyList(MatchResult.class)
          .value(values -> Assertions.assertTrue(
                  values
                      .stream()
                      .reduce(false, (acc, m) -> acc || m.fuzzy(), (a, b) -> a || b)
              )
          );
    } else {
      response
          .expectStatus()
          .isNoContent();
    }


  }

  private static Stream<Arguments> individualMatch() {
    return Stream.of(
        Arguments.of(
            ClientMatchDataGenerator.getAddress(),
            "[]",
            "[]",
            "[]",
            "[]",
            "[]",
            false,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator.getAddress(),
            "[]",
            "[]",
            "[]",
            "[]",
            "[{\"clientNumber\":\"00000001\"}]",
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator.getAddress(),
            "[]",
            "[{\"clientNumber\":\"00000002\"}]",
            "[]",
            "[]",
            "[]",
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator.getAddress(),
            "[{\"clientNumber\":\"00000003\"}]",
            "[]",
            "[]",
            "[]",
            "[]",
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator.getAddress(),
            "[]",
            "[{\"clientNumber\":\"00000005\"}]",
            "[]",
            "[]",
            "[]",
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator.getAddress(),
            "[]",
            "[]",
            "[{\"clientNumber\":\"00000008\"}]",
            "[]",
            "[]",
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator.getAddress(),
            "[]",
            "[]",
            "[]",
            "[{\"clientNumber\":\"00000008\"}]",
            "[]",
            true,
            false
        )
    );
  }

}