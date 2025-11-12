package ca.bc.gov.app.controller.client;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Named.named;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import reactor.core.publisher.Mono;

@DisplayName("Integrated Test | Client Match for Individual Controller")
class ClientMatchIndividualControllerIntegrationTest extends AbstractTestContainerIntegrationTest {

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

  @Autowired
  protected WebTestClient client;

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

  @ParameterizedTest(name = "{0}")
  @MethodSource("individualMatch")
  @DisplayName("List and Search")
  void shouldRunMatch(
      ClientSubmissionDto dto,
      String individualFuzzyMatch,
      String individualFullMatch,
      String documentMatch,
      boolean error,
      boolean fuzzy
  ) {
    legacyStub.resetAll();

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/individual"))
                .withQueryParam("firstName", equalTo(dto.businessInformation().firstName()))
                .withQueryParam("lastName", equalTo(dto.businessInformation().lastName()))
                .withQueryParam("dob", equalTo(dto.businessInformation().birthdate().format(
                    DateTimeFormatter.ISO_DATE))
                )
                .willReturn(okJson(individualFuzzyMatch))
        );

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/individual"))
                .withQueryParam("firstName", equalTo(dto.businessInformation().firstName()))
                .withQueryParam("lastName", equalTo(dto.businessInformation().lastName()))
                .withQueryParam("dob", equalTo(dto.businessInformation().birthdate().format(
                    DateTimeFormatter.ISO_DATE))
                )
                .withQueryParam("identification",
                    equalTo(dto.businessInformation().clientIdentification()))
                .willReturn(okJson(individualFullMatch))
        );

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/id/" + dto.businessInformation().idType() + "/"
                + dto.businessInformation().clientIdentification()))
                .willReturn(okJson(documentMatch))
        );

    ResponseSpec response =
        client
            .post()
            .uri("/api/clients/matches")
            .header("X-STEP", "1")
            .body(Mono.just(dto), ClientSubmissionDto.class)
            .exchange();

    if (error) {
      response
          .expectStatus()
          .isEqualTo(HttpStatus.CONFLICT)
          .expectBodyList(MatchResult.class)
          .value(values -> Assertions.assertEquals(
                  fuzzy,
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
            named("no match",
                ClientMatchDataGenerator.getIndividualDto(
                    "Jhon",
                    "Wick",
                    LocalDate.of(1970, 1, 1),
                    "CDDL",
                    "BC",
                    "1234567"
                )
            ),
            "[]",
            "[]",
            "[]",
            false,
            false
        ),
        Arguments.of(
            named("document match",
                ClientMatchDataGenerator.getIndividualDto(
                    "James",
                    "Wick",
                    LocalDate.of(1970, 1, 1),
                    "CDDL",
                    "AB",
                    "7654321"
                )
            ),
            "[]",
            "[]",
            "[{\"clientNumber\":\"00000001\"}]",
            true,
            false
        ),
        Arguments.of(
            named("full match",
                ClientMatchDataGenerator.getIndividualDto(
                    "Valeria",
                    "Valid",
                    LocalDate.of(1970, 1, 1),
                    "CDDL",
                    "YK",
                    "1233210"
                )
            ),
            "[]",
            "[{\"clientNumber\":\"00000002\"}]",
            "[]",
            true,
            true
        ),
        Arguments.of(
            named("fuzzy match",
                ClientMatchDataGenerator.getIndividualDto(
                    "Papernon",
                    "Pompadour",
                    LocalDate.of(1970, 1, 1),
                    "CDDL",
                    "ON",
                    "9994545"
                )
            ),
            "[{\"clientNumber\":\"00000003\"}]",
            "[]",
            "[]",
            true,
            true
        ),
        Arguments.of(
            named("full and fuzzy match",
                ClientMatchDataGenerator.getIndividualDto(
                    "Karls",
                    "Enrikvinjon",
                    LocalDate.of(1970, 1, 1),
                    "CDDL",
                    "BC",
                    "3337474"
                )
            ),
            "[{\"clientNumber\":\"00000004\"}]",
            "[{\"clientNumber\":\"00000005\"}]",
            "[]",
            true,
            true //It used to be false, but as now the fields have different values, it should be true
        ),
        Arguments.of(
            named("all match",
                ClientMatchDataGenerator.getIndividualDto(
                    "Palitz",
                    "Yelvengard",
                    LocalDate.of(1970, 1, 1),
                    "USDL",
                    "AZ",
                    "7433374"
                )
            ),
            "[{\"clientNumber\":\"00000006\"}]",
            "[{\"clientNumber\":\"00000007\"}]",
            "[{\"clientNumber\":\"00000008\"}]",
            true,
            true //It used to be false, but as now the fields have different values, it should be true
        )
    );
  }

}