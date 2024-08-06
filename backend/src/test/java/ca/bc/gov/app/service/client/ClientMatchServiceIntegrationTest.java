package ca.bc.gov.app.service.client;

import static ca.bc.gov.app.extensions.ClientMatchDataGenerator.getDto;
import static ca.bc.gov.app.extensions.ClientMatchDataGenerator.getIndividualDto;
import static ca.bc.gov.app.extensions.ClientMatchDataGenerator.getRandomData;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Named.named;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.MatchResult;
import ca.bc.gov.app.exception.DataMatchException;
import ca.bc.gov.app.exception.InvalidRequestObjectException;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.NotImplementedException;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

@DisplayName("Integrated Test | Client Match Service")
class ClientMatchServiceIntegrationTest extends AbstractTestContainerIntegrationTest {

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
  private ClientMatchService service;

  @DisplayName("Should fail for invalid cases")
  @ParameterizedTest(name = "Case Step {1}: {0} will throw {2}")
  @MethodSource("invalidCases")
  void shouldFailForInvalidCases(
      ClientSubmissionDto dto,
      int step,
      Class<RuntimeException> exceptionClass
  ) {

    service
        .matchClients(dto, step)
        .as(StepVerifier::create)
        .expectError(exceptionClass)
        .verify();

  }

  @DisplayName("Matching individuals")
  @ParameterizedTest
  @MethodSource("individualMatch")
  void shouldMatchIndividuals(
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
                .withQueryParam("lastName", equalTo(dto.businessInformation().businessName()))
                .withQueryParam("dob", equalTo(dto.businessInformation().birthdate().format(
                    DateTimeFormatter.ISO_DATE))
                )
                .willReturn(okJson(individualFuzzyMatch))
        );

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/individual"))
                .withQueryParam("firstName", equalTo(dto.businessInformation().firstName()))
                .withQueryParam("lastName", equalTo(dto.businessInformation().businessName()))
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

    StepVerifier.FirstStep<Void> matcher =
        service
            .matchClients(dto, 1)
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

  private static Stream<Arguments> invalidCases() {
    return Stream
        .of(
            Arguments.of(
                named("Null ClientSubmissionDto", null),
                1,
                named("Invalid Request", InvalidRequestObjectException.class)
            ),

            Arguments.of(
                named("Null content of ClientSubmissionDto",
                    new ClientSubmissionDto(
                        null,
                        null,
                        null,
                        null
                    )
                ),
                1,
                named("Invalid Request", InvalidRequestObjectException.class)
            ),

            Arguments.of(
                named("getDto()", getDto(null, null)),
                1,
                named("Invalid Request", InvalidRequestObjectException.class)
            ),

            Arguments.of(
                named("Individual random data and null location",
                    getRandomData("I").withLocation(null)),
                1,
                named("Invalid Request", InvalidRequestObjectException.class)
            ),

            Arguments.of(
                named("Individual random data and null content location", getRandomData("I")),
                2,
                named("Invalid Request", InvalidRequestObjectException.class)
            ),
            Arguments.of(
                named("Individual random with invalid address on list",
                    getRandomData("I",
                        new ClientAddressDto(
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
                            0,
                            null
                        ),
                        null
                    )
                ),
                2,
                named("Invalid Request", InvalidRequestObjectException.class)
            ),
            Arguments.of(
                named("Individual random data and null content location", getRandomData("I")),
                3,
                named("Invalid Request", InvalidRequestObjectException.class)
            ),
            Arguments.of(
                named("Individual random with invalid contact on list",
                    getRandomData("I", null,
                        new ClientContactDto(
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            0,
                            null
                        )
                    )
                ),
                3,
                named("Invalid Request", InvalidRequestObjectException.class)
            ),
            Arguments.of(
                named("Random individual with wrong step", getRandomData("I")),
                4,
                named("Invalid Request", InvalidRequestObjectException.class)
            ),
            //TODO: add invalid BC Registered cases
            Arguments.of(
                named("Random First Nation", getRandomData("R")),
                1,
                named("Not Implemented", NotImplementedException.class)
            ),
            Arguments.of(
                named("Random Government", getRandomData("G")),
                1,
                named("Not Implemented", NotImplementedException.class)
            ),
            Arguments.of(
                named("Random Forest", getRandomData("F")),
                1,
                named("Not Implemented", NotImplementedException.class)
            ),
            Arguments.of(
                named("Random Unregistered", getRandomData("U")),
                1,
                named("Not Implemented", NotImplementedException.class)
            ),
            Arguments.of(
                named("Random Invalid type", getRandomData("J")),
                1,
                named("Invalid Request", InvalidRequestObjectException.class)
            )
        );
  }


  private static Stream<Arguments> individualMatch() {
    return Stream.of(
        Arguments.of(
            getIndividualDto(
                "Jhon",
                "Wick",
                LocalDate.of(1970, 1, 1),
                "CDDL",
                "BC",
                "1234567"
            ),
            "[]",
            "[]",
            "[]",
            false,
            false
        ),
        Arguments.of(
            getIndividualDto(
                "James",
                "Wick",
                LocalDate.of(1970, 1, 1),
                "CDDL",
                "AB",
                "7654321"
            ),
            "[]",
            "[]",
            "[{\"clientNumber\":\"00000001\"}]",
            true,
            false
        ),
        Arguments.of(
            getIndividualDto(
                "Valeria",
                "Valid",
                LocalDate.of(1970, 1, 1),
                "CDDL",
                "YK",
                "1233210"
            ),
            "[]",
            "[{\"clientNumber\":\"00000002\"}]",
            "[]",
            true,
            false
        ),
        Arguments.of(
            getIndividualDto(
                "Papernon",
                "Pompadour",
                LocalDate.of(1970, 1, 1),
                "CDDL",
                "ON",
                "9994545"
            ),
            "[{\"clientNumber\":\"00000003\"}]",
            "[]",
            "[]",
            true,
            true
        ),
        Arguments.of(
            getIndividualDto(
                "Karls",
                "Enrikvinjon",
                LocalDate.of(1970, 1, 1),
                "CDDL",
                "BC",
                "3337474"
            ),
            "[{\"clientNumber\":\"00000004\"}]",
            "[{\"clientNumber\":\"00000005\"}]",
            "[]",
            true,
            false
        ),
        Arguments.of(
            getIndividualDto(
                "Palitz",
                "Yelvengard",
                LocalDate.of(1970, 1, 1),
                "USDL",
                "AZ",
                "7433374"
            ),
            "[{\"clientNumber\":\"00000006\"}]",
            "[{\"clientNumber\":\"00000007\"}]",
            "[{\"clientNumber\":\"00000008\"}]",
            true,
            false
        )
    );
  }

}