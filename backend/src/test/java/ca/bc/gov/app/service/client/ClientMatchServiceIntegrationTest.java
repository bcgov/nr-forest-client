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
import ca.bc.gov.app.extensions.ClientMatchDataGenerator;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Condition;
import org.assertj.core.api.InstanceOfAssertFactories;
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
  @ParameterizedTest(name = "[{index}] Case Step {1}: {0} will throw {2}")
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
  @ParameterizedTest(name = "{0}")
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
                  .asInstanceOf(InstanceOfAssertFactories.list(Object.class))
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

  @DisplayName("Matching registered clients")
  @ParameterizedTest
  @MethodSource("registeredMatch")
  void shouldMatchRegistered(
      ClientSubmissionDto dto,
      String individualFuzzyMatch,
      String clientNameFuzzyMatch,
      String clientRegistrationFullMatch,
      String fullNameMatch,
      String acronymMatch,
      String dbaFuzzyMatch,
      String dbaFullMatch,
      boolean error,
      boolean fuzzy
  ) {

    legacyStub.resetAll();

    if (dto.businessInformation().clientType().equalsIgnoreCase("RSP")) {
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
    }

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/registrationOrName"))
                .withQueryParam("registrationNumber",
                    equalTo(dto.businessInformation().registrationNumber()))
                .willReturn(okJson(clientRegistrationFullMatch))
        );

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/clientName"))
                .withQueryParam("clientName", equalTo(dto.businessInformation().businessName()))
                .willReturn(okJson(fullNameMatch))
        );

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/match"))
                .withQueryParam("companyName", equalTo(dto.businessInformation().businessName()))
                .willReturn(okJson(clientNameFuzzyMatch))
        );

    if (StringUtils.isNotBlank(dto.businessInformation().clientAcronym())) {
      legacyStub
          .stubFor(
              get(urlPathEqualTo("/api/search/acronym"))
                  .withQueryParam("acronym", equalTo(dto.businessInformation().clientAcronym()))
                  .willReturn(okJson(acronymMatch))
          );
    }

    if (StringUtils.isNotBlank(dto.businessInformation().doingBusinessAs())) {
      legacyStub
          .stubFor(
              get(urlPathEqualTo("/api/search/doingBusinessAs"))
                  .withQueryParam("dbaName", equalTo(dto.businessInformation().doingBusinessAs()))
                  .willReturn(okJson(dbaFuzzyMatch))
          );

      legacyStub
          .stubFor(
              get(urlPathEqualTo("/api/search/doingBusinessAs"))
                  .withQueryParam("dbaName", equalTo(dto.businessInformation().doingBusinessAs()))
                  .withQueryParam("isFuzzy", equalTo("false"))
                  .willReturn(okJson(dbaFullMatch))
          );
    }

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
                  .asInstanceOf(InstanceOfAssertFactories.list(Object.class))
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

  @DisplayName("Matching other types")
  @ParameterizedTest(name = "[{index}] error {4} fuzzy {5} when provided with {1}, {2}, {3}")
  @MethodSource("otherMatch")
  void shouldMatchOthers(
      ClientSubmissionDto dto,
      String clientNameFuzzyMatch,
      String fullNameMatch,
      String acronymMatch,
      boolean error,
      boolean fuzzy
  ) {

    legacyStub.resetAll();

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/clientName"))
                .withQueryParam("clientName", equalTo(dto.businessInformation().businessName()))
                .willReturn(okJson(fullNameMatch))
        );

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/match"))
                .withQueryParam("companyName", equalTo(dto.businessInformation().businessName()))
                .willReturn(okJson(clientNameFuzzyMatch))
        );

    if (StringUtils.isNotBlank(dto.businessInformation().clientAcronym())) {
      legacyStub
          .stubFor(
              get(urlPathEqualTo("/api/search/acronym"))
                  .withQueryParam("acronym", equalTo(dto.businessInformation().clientAcronym()))
                  .willReturn(okJson(acronymMatch))
          );
    }

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
                  .asInstanceOf(InstanceOfAssertFactories.list(Object.class))
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

  @DisplayName("Matching first nations")
  @ParameterizedTest(name = "[{index}] error {5} fuzzy {6} when provided with {1}, {2}, {3}, {4}")
  @MethodSource("firstNationsMatch")
  void shouldMatchFirstNations(
      ClientSubmissionDto dto,
      String clientNameFuzzyMatch,
      String fullNameMatch,
      String acronymMatch,
      String clientRegistrationFullMatch,
      boolean error,
      boolean fuzzy
  ) {

    legacyStub.resetAll();

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/clientName"))
                .withQueryParam("clientName", equalTo(dto.businessInformation().businessName()))
                .willReturn(okJson(fullNameMatch))
        );

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/match"))
                .withQueryParam("companyName", equalTo(dto.businessInformation().businessName()))
                .willReturn(okJson(clientNameFuzzyMatch))
        );

    if (StringUtils.isNotBlank(dto.businessInformation().clientAcronym())) {
      legacyStub
          .stubFor(
              get(urlPathEqualTo("/api/search/acronym"))
                  .withQueryParam("acronym", equalTo(dto.businessInformation().clientAcronym()))
                  .willReturn(okJson(acronymMatch))
          );
    }

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/registrationOrName"))
                .withQueryParam("registrationNumber",
                    equalTo(dto.businessInformation().registrationNumber()))
                .willReturn(okJson(clientRegistrationFullMatch))
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
                  .asInstanceOf(InstanceOfAssertFactories.list(Object.class))
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
            named("no matches",
                getIndividualDto(
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
            named("doc match",
                getIndividualDto(
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
                getIndividualDto(
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
                getIndividualDto(
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
            named("fuzzy and full",
                getIndividualDto(
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
            true
        ),
        Arguments.of(
            named("all matches",
                getIndividualDto(
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
            false
        )
    );
  }

  private static Stream<Arguments> registeredMatch() {
    return Stream.of(
        Arguments.of(
            ClientMatchDataGenerator
                .getRegistered(
                    "C123456",
                    "Fake Corp",
                    "C",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "C"
                ),
            named("no individual", "[]"),
            named("no fuzzy name", "[]"),
            named("no registration", "[]"),
            named("no full name", "[]"),
            named("no acronym", "[]"),
            named("no dba fuzzy", "[]"),
            named("no dba full", "[]"),
            false,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getRegisteredSP(
                    "C123456",
                    "Fake Corp",
                    StringUtils.EMPTY,
                    "Fake Corp",
                    StringUtils.EMPTY,
                    "Johnathan",
                    "Wick",
                    LocalDate.of(1970, 1, 12)
                ),
            named("individual matched", "[{\"clientNumber\":\"00000001\"}]"),
            named("no fuzzy name", "[]"),
            named("no registration", "[]"),
            named("no full name", "[]"),
            named("no acronym", "[]"),
            named("no dba fuzzy", "[]"),
            named("no dba full", "[]"),
            true,
            true
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getRegistered(
                    "S123456",
                    "Fake Corp",
                    "C",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "S"
                ),
            named("no individual", "[]"),
            named("no fuzzy name", "[]"),
            named("registration matched", "[{\"clientNumber\":\"00000001\"}]"),
            named("no full name", "[]"),
            named("no acronym", "[]"),
            named("no dba fuzzy", "[]"),
            named("no dba full", "[]"),
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getRegistered(
                    "A123456",
                    "Fake Corp",
                    "C",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "FAKE",
                    "A"
                ),
            named("no individual", "[]"),
            named("no fuzzy name", "[]"),
            named("no registration", "[]"),
            named("no full name", "[]"),
            named("acronym matched", "[{\"clientNumber\":\"00000001\"}]"),
            named("no dba fuzzy", "[]"),
            named("no dba full", "[]"),
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getRegistered(
                    "P123456",
                    "Fake Corp",
                    "C",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "P"
                ),
            named("no individual", "[]"),
            named("fuzzy name matched", "[{\"clientNumber\":\"00000001\"}]"),
            named("no registration", "[]"),
            named("no full name", "[]"),
            named("no acronym", "[]"),
            named("no dba fuzzy", "[]"),
            named("no dba full", "[]"),
            true,
            true
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getRegistered(
                    "L123456",
                    "Fake Corp",
                    "C",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "L"
                ),
            named("no individual", "[]"),
            named("fuzzy name matched", "[{\"clientNumber\":\"00000001\"}]"),
            named("no registration", "[]"),
            named("full name matched", "[{\"clientNumber\":\"00000002\"}]"),
            named("no acronym", "[]"),
            named("no dba fuzzy", "[]"),
            named("no dba full", "[]"),
            true,
            true
        ),

        Arguments.of(
            ClientMatchDataGenerator
                .getRegistered(
                    "C123456",
                    "Fake Corp",
                    "C",
                    StringUtils.EMPTY,
                    "Fake Corp",
                    StringUtils.EMPTY,
                    "C"
                ),
            named("no individual", "[]"),
            named("no fuzzy name", "[]"),
            named("no registration", "[]"),
            named("no full name", "[]"),
            named("no acronym", "[]"),
            named("no dba fuzzy", "[{\"clientNumber\":\"00000001\"}]"),
            named("no dba full", "[]"),
            true,
            true
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getRegistered(
                    "C123456",
                    "Fake Corp",
                    "C",
                    StringUtils.EMPTY,
                    "Fake Corp",
                    StringUtils.EMPTY,
                    "C"
                ),
            named("no individual", "[]"),
            named("no fuzzy name", "[]"),
            named("no registration", "[]"),
            named("no full name", "[]"),
            named("no acronym", "[]"),
            named("dba fuzzy matched", "[{\"clientNumber\":\"00000001\"}]"),
            named("dba full matched", "[{\"clientNumber\":\"00000001\"}]"),
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getRegistered(
                    "C123456",
                    "Fake Corp",
                    "C",
                    StringUtils.EMPTY,
                    "Fake Corp",
                    StringUtils.EMPTY,
                    "C"
                ),
            named("no individual", "[]"),
            named("fuzzy name matched", "[{\"clientNumber\":\"00000001\"}]"),
            named("no registration", "[]"),
            named("full name matched", "[{\"clientNumber\":\"00000002\"}]"),
            named("no acronym", "[]"),
            named("dba fuzzy matched", "[{\"clientNumber\":\"00000003\"}]"),
            named("dba full matched", "[{\"clientNumber\":\"00000004\"}]"),
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getRegisteredSP(
                    "C123456",
                    "Fake Corp",
                    StringUtils.EMPTY,
                    "Fake Corp",
                    "FAKE",
                    "Johnathan",
                    "Wick",
                    LocalDate.of(1970, 1, 12)
                ),
            named("individual matched", "[{\"clientNumber\":\"00000001\"}]"),
            named("fuzzy name matched", "[{\"clientNumber\":\"00000002\"}]"),
            named("registration matched", "[{\"clientNumber\":\"00000003\"}]"),
            named("full name matched", "[{\"clientNumber\":\"00000004\"}]"),
            named("acronym matched", "[{\"clientNumber\":\"00000005\"}]"),
            named("dba fuzzy matched", "[{\"clientNumber\":\"00000006\"}]"),
            named("dba full matched", "[{\"clientNumber\":\"00000007\"}]"),
            true,
            false
        )
    );
  }

  private static Stream<Arguments> otherMatch() {
    return Stream.of(
        Arguments.of(
            ClientMatchDataGenerator
                .getOther(
                    "Government",
                    null,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "G"
                ),
            named("no fuzzy name", "[]"),
            named("no full name", "[]"),
            named("no acronym", "[]"),
            false,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getOther(
                    "Forest",
                    null,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "F"
                ),
            named("fuzzy name matched",
                "[{\"clientNumber\":\"00000001\"}]"),
            named("no full name", "[]"),
            named("no acronym", "[]"),
            true,
            true
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getOther(
                    "Unregistered",
                    null,
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "U"
                ),
            named("no fuzzy name", "[]"),
            named("full name matched",
                "[{\"clientNumber\":\"00000002\"}]"),
            named("no acronym", "[]"),
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getOther(
                    "Government2",
                    null,
                    StringUtils.EMPTY,
                    "ABC",
                    "G"
                ),
            named("no fuzzy name", "[]"),
            named("no full name", "[]"),
            named("acronym matched",
                "[{\"clientNumber\":\"00000003\"}]"),
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getOther(
                    "Government3",
                    null,
                    StringUtils.EMPTY,
                    "ABC",
                    "G"
                ),
            named("fuzzy name matched",
                "[{\"clientNumber\":\"00000001\"}]"),
            named("full name matched",
                "[{\"clientNumber\":\"00000002\"}]"),
            named("acronym matched",
                "[{\"clientNumber\":\"00000003\"}]"),
            true,
            false
        )
    );
  }

  private static Stream<Arguments> firstNationsMatch() {
    return Stream.of(
        Arguments.of(
            ClientMatchDataGenerator
                .getFirstNations(
                    "First Nation Band",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "B",
                    "DINA123"
                ),
            named("no fuzzy name", "[]"),
            named("no full name", "[]"),
            named("no acronym", "[]"),
            named("no band number", "[]"),
            false,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getFirstNations(
                    "First Nation Tribe",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "T",
                    "DINA123"
                ),
            named("fuzzy name matched",
                "[{\"clientNumber\":\"00000001\"}]"),
            named("no full name", "[]"),
            named("no acronym", "[]"),
            named("no band number", "[]"),
            true,
            true
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getFirstNations(
                    "First Nation",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "R",
                    "DINA123"
                ),
            named("no fuzzy name", "[]"),
            named("full name matched",
                "[{\"clientNumber\":\"00000002\"}]"),
            named("no acronym", "[]"),
            named("no band number", "[]"),
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getFirstNations(
                    "First Nation Band",
                    StringUtils.EMPTY,
                    "TUPI",
                    "B",
                    "DINA123"
                ),
            named("no fuzzy name", "[]"),
            named("no full name", "[]"),
            named("acronym matched",
                "[{\"clientNumber\":\"00000003\"}]"),
            named("no band number", "[]"),
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getFirstNations(
                    "First Nation Band",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "B",
                    "DINA123"
                ),
            named("no fuzzy name", "[]"),
            named("no full name", "[]"),
            named("no acronym", "[]"),
            named("band number matched",
                "[{\"clientNumber\":\"00000004\"}]"),
            true,
            false
        ),
        Arguments.of(
            ClientMatchDataGenerator
                .getFirstNations(
                    "First Nation Band",
                    StringUtils.EMPTY,
                    "TUPI",
                    "B",
                    "DINA123"
                ),
            named("fuzzy name matched",
                "[{\"clientNumber\":\"00000001\"}]"),
            named("full name matched",
                "[{\"clientNumber\":\"00000002\"}]"),
            named("acronym matched",
                "[{\"clientNumber\":\"00000003\"}]"),
            named("band number matched",
                "[{\"clientNumber\":\"00000004\"}]"),
            true,
            false
        )
    );
  }

}