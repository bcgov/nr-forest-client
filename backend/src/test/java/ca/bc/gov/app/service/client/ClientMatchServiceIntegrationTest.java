package ca.bc.gov.app.service.client;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.MatchResult;
import ca.bc.gov.app.exception.DataMatchException;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
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
  @ParameterizedTest
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
                .withQueryParam("identification", equalTo(dto.businessInformation().idValue()))
                .willReturn(okJson(individualFullMatch))
        );

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/id/" + dto.businessInformation().idType() + "/"
                + dto.businessInformation().idValue()))
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
                null,
                1,
                IllegalArgumentException.class
            ),
            Arguments.of(
                new ClientSubmissionDto(
                    null,
                    null,
                    null,
                    null
                ),
                1,
                IllegalArgumentException.class
            ),
            Arguments.of(
                getDto(),
                1,
                IllegalArgumentException.class
            ),
            Arguments.of(
                getRandomData().withLocation(null),
                1,
                IllegalArgumentException.class
            ),
            Arguments.of(
                getRandomData(),
                2,
                NotImplementedException.class
            ),
            Arguments.of(
                getRandomData(),
                3,
                NotImplementedException.class
            ),
            Arguments.of(
                getRandomData(),
                4,
                IllegalArgumentException.class
            ),
            Arguments.of(
                getRandomData()
                    .withBusinessInformation(
                        getRandomData()
                            .businessInformation()
                            .withClientType("BCR")
                    ),
                1,
                NotImplementedException.class
            ),
            Arguments.of(
                getRandomData()
                    .withBusinessInformation(
                        getRandomData()
                            .businessInformation()
                            .withClientType("R")
                    ),
                1,
                NotImplementedException.class
            ),
            Arguments.of(
                getRandomData()
                    .withBusinessInformation(
                        getRandomData()
                            .businessInformation()
                            .withClientType("G")
                    ),
                1,
                NotImplementedException.class
            ),
            Arguments.of(
                getRandomData()
                    .withBusinessInformation(
                        getRandomData()
                            .businessInformation()
                            .withClientType("F")
                    ),
                1,
                NotImplementedException.class
            ),
            Arguments.of(
                getRandomData()
                    .withBusinessInformation(
                        getRandomData()
                            .businessInformation()
                            .withClientType("U")
                    ),
                1,
                NotImplementedException.class
            ),
            Arguments.of(
                getRandomData()
                    .withBusinessInformation(
                        getRandomData()
                            .businessInformation()
                            .withClientType("J")
                    ),
                1,
                IllegalArgumentException.class
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
                "BCDL",
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
                "ABDL",
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
                "YKDL",
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
                "ONDL",
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
                "BCDL",
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
                "NLDL",
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

  private static ClientSubmissionDto getIndividualDto(
      String firstName,
      String lastName,
      LocalDate birthdate,
      String idType,
      String idValue
  ) {

    ClientSubmissionDto dto = getDtoType("I");

    return
        dto
            .withBusinessInformation(
                dto
                    .businessInformation()
                    .withBusinessName(lastName)
                    .withFirstName(firstName)
                    .withBirthdate(birthdate)
                    .withIdType(idType)
                    .withIdValue(idValue)
                    .withClientType("I")
            );
  }

  private static ClientSubmissionDto getRandomData() {
    return getIndividualDto(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        LocalDate.now(),
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString()
    );
  }

  private static ClientSubmissionDto getDtoType(String type) {
    ClientSubmissionDto dto = getDto();
    return dto.withBusinessInformation(
        dto
            .businessInformation()
            .withClientType(type)
    );

  }

  private static ClientSubmissionDto getDto() {
    return new ClientSubmissionDto(
        new ClientBusinessInformationDto(
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
        ),
        new ClientLocationDto(
            null,
            null
        ),
        null,
        null
    );
  }

}