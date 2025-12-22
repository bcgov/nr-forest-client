package ca.bc.gov.app.controller.client;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.BcRegistryTestConstants;
import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;

@DisplayName("Integrated Test | FSA Client Controller")
class ClientControllerIntegrationTest extends AbstractTestContainerIntegrationTest {

  @RegisterExtension
  static WireMockExtension bcRegistryStub = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10040)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();

  @RegisterExtension
  static WireMockExtension chesStub = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10010)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();

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
    bcRegistryStub.resetAll();
    chesStub.resetAll();

    chesStub
        .stubFor(
            post("/chess/uri")
                .willReturn(
                    ok(TestConstants.CHES_SUCCESS_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );

    chesStub
        .stubFor(
            post("/chess/uri/email")
                .willReturn(
                    ok(TestConstants.CHES_SUCCESS_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );

    chesStub
        .stubFor(
            post("/token/uri")
                .willReturn(
                    ok(TestConstants.CHES_TOKEN_MESSAGE)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                )
        );

    client = client.mutate()
        .responseTimeout(Duration.ofSeconds(10))
        .build();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("clientDetailing")
  @DisplayName("Client details")
  void shouldGetClientDetails(
      String clientNumber,

      int detailsStatus,
      String detailsResponse,

      int addressStatus,
      String addressResponse,

      int responseStatus,
      String responseContent,

      String legacyResponse
  ) {

    reset();

    bcRegistryStub
        .stubFor(
            post(urlPathEqualTo("/registry-search/api/v2/search/businesses"))
                .willReturn(okJson(
                        BcRegistryTestConstants.BCREG_FACET_ANY
                            .replace("C0123456", clientNumber)
                            .replace("EXAMPLE COMPANY LTD.", "SAMPLE COMPANY")
                    )
                )
        );

    bcRegistryStub
        .stubFor(post(urlPathEqualTo(
            "/registry-search/api/v1/businesses/" + clientNumber + "/documents/requests"))
            .withHeader("x-apikey", equalTo("abc1234"))
            .withHeader("Account-Id", equalTo("account 0000"))
            .withRequestBody(equalToJson(TestConstants.BCREG_DOC_REQ))
            .willReturn(
                status(detailsStatus)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(detailsResponse)
            )
        );

    bcRegistryStub
        .stubFor(get(urlPathEqualTo(
            "/registry-search/api/v1/businesses/" + clientNumber + "/documents/aa0a00a0a"))
            .withHeader("x-apikey", equalTo("abc1234"))
            .withHeader("Account-Id", equalTo("account 0000"))
            .willReturn(
                status(addressStatus)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(addressResponse)
            )
        );

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/registrationOrName"))
                .withQueryParam("registrationNumber", equalTo("AA0000001"))
                .withQueryParam("companyName", equalTo("SAMPLE COMPANY"))
                .willReturn(okJson(legacyResponse))
        );

    WebTestClient.BodyContentSpec response =
        client
            .mutateWith(csrf())
            .mutateWith(
                mockJwt()
                    .jwt(jwt -> jwt.claims(
                        claims -> claims.putAll(TestConstants.getClaims("bceidbusiness"))))
                    .authorities(new SimpleGrantedAuthority(
                        "ROLE_" + ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER))
            )
            .get()
            .uri("/api/clients/{clientNumber}", Map.of("clientNumber", clientNumber))
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.valueOf(responseStatus))
            .expectBody()
            .consumeWith(System.out::println);

    if (HttpStatus.valueOf(responseStatus).is2xxSuccessful()) {
      response.json(responseContent);
    } else {
      response.equals(responseContent);
    }

  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("clientDetailingStaff")
  @DisplayName("Client details for staff")
  void shouldGetClientDetailsForStaff(
      String clientNumber,

      int bcRegistryDocRequestStatus,
      String bcRegistryDocRequestResponse,

      int bcRegistryDocDetailsStatus,
      String bcRegistryDocDetailsResponse,

      String legacyResponse,

      int responseStatus,
      String responseContent
  ) {

    reset();

    bcRegistryStub
        .stubFor(
            post(urlPathEqualTo("/registry-search/api/v2/search/businesses"))
                .willReturn(okJson(
                        BcRegistryTestConstants.BCREG_FACET_ANY
                            .replace("C0123456", clientNumber)
                            .replace("EXAMPLE COMPANY LTD.", "SAMPLE COMPANY")
                    )
                )
        );

    bcRegistryStub
        .stubFor(post(urlPathEqualTo(
            "/registry-search/api/v1/businesses/" + clientNumber + "/documents/requests"))
            .withHeader("x-apikey", equalTo("abc1234"))
            .withHeader("Account-Id", equalTo("account 0000"))
            .withRequestBody(equalToJson(TestConstants.BCREG_DOC_REQ))
            .willReturn(
                status(bcRegistryDocRequestStatus)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(bcRegistryDocRequestResponse)
            )
        );

    bcRegistryStub
        .stubFor(get(urlPathEqualTo(
            "/registry-search/api/v1/businesses/" + clientNumber + "/documents/aa0a00a0a"))
            .withHeader("x-apikey", equalTo("abc1234"))
            .withHeader("Account-Id", equalTo("account 0000"))
            .willReturn(
                status(bcRegistryDocDetailsStatus)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(bcRegistryDocDetailsResponse)
            )
        );

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/registrationOrName"))
                .withQueryParam("registrationNumber", equalTo("AA0000001"))
                .withQueryParam("companyName", equalTo("SAMPLE COMPANY"))
                .willReturn(okJson(legacyResponse))
        );

    WebTestClient.BodyContentSpec response =
        client
            .mutateWith(csrf())
            .mutateWith(mockUser().roles(ApplicationConstant.ROLE_EDITOR))
            .mutateWith(
                mockJwt()
                    .jwt(
                        jwt -> jwt.claims(claims -> claims.putAll(TestConstants.getClaims("idir"))))
                    .authorities(
                        new SimpleGrantedAuthority("ROLE_" + ApplicationConstant.ROLE_EDITOR))
            )
            .get()
            .uri("/api/clients/{clientNumber}", Map.of("clientNumber", clientNumber))
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.valueOf(responseStatus))
            .expectBody()
            .consumeWith(System.out::println);

    if (HttpStatus.valueOf(responseStatus).is2xxSuccessful()) {
      response.json(responseContent);
    } else {
      response.equals(responseContent);
    }

  }

  @Test
  @DisplayName("Look for Incorporation with ID 'BC0772006'")
  void shouldGetDataFromIncorporation() {

    bcRegistryStub
        .stubFor(
            post(urlPathEqualTo("/registry-search/api/v2/search/businesses"))
                .withRequestBody(equalToJson("""
                        {
                           "query" : {
                             "value" : "BC0772006",
                             "identifier" : "BC0772006"
                           },
                           "categories" : {
                             "status" : [ "ACTIVE" ]
                           },
                           "rows" : 100,
                           "start" : 0
                         }"""
                    )
                )
                .willReturn(okJson(TestConstants.ORGBOOK_INCORP_OK))
        );

    client
        .mutateWith(csrf())
        .mutateWith(
            mockJwt()
                .jwt(jwt -> jwt.claims(
                    claims -> claims.putAll(TestConstants.getClaims("bceidbusiness"))))
                .authorities(new SimpleGrantedAuthority(
                    "ROLE_" + ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER))
        )
        .get()
        .uri("/api/clients/incorporation/BC0772006")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .json(TestConstants.ORGBOOK_INCORP_OK_RESPONSE);

  }

  @Test
  @DisplayName("Look for Incorporation with ID 'BC0000000'")
  void shouldGetNoDataFromIncorporation() {

    bcRegistryStub
        .stubFor(
            post(urlPathEqualTo("/registry-search/api/v2/search/businesses"))
                .withRequestBody(equalToJson("""
                        {
                          "query": { "value": "BC0000000","identifier": "BC0000000" },
                          "categories":{ "status":["ACTIVE"] },
                          "rows": 100,
                          "start":0
                        }"""
                    )
                )
                .willReturn(okJson(TestConstants.ORGBOOK_INCORP_EMPTY))
        );

    client
        .mutateWith(csrf())
        .mutateWith(
            mockJwt()
                .jwt(jwt -> jwt.claims(
                    claims -> claims.putAll(TestConstants.getClaims("bceidbusiness"))))
                .authorities(new SimpleGrantedAuthority(
                    "ROLE_" + ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER))
        )
        .get()
        .uri("/api/clients/incorporation/BC0000000")
        .exchange()
        .expectStatus().isNotFound()
        .expectBody(String.class)
        .isEqualTo("No data found for client number BC0000000");

  }

  @Test
  @DisplayName("Look for name 'Power Corp'")
  void shouldGetDataFromNameLookup() {

    bcRegistryStub
        .stubFor(
            post(urlPathEqualTo("/registry-search/api/v2/search/businesses"))
                .withRequestBody(equalToJson("""
                        {
                          "query": { "value": "Power","name": "Power" },
                          "categories":{ "status":["ACTIVE"] },
                          "rows": 100,
                          "start":0
                        }"""
                    )
                )
                .willReturn(okJson(TestConstants.ORGBOOK_INCORP_OK))
        );

    client
        .mutateWith(csrf())
        .mutateWith(
            mockJwt()
                .jwt(jwt -> jwt.claims(
                    claims -> claims.putAll(TestConstants.getClaims("bceidbusiness"))))
                .authorities(new SimpleGrantedAuthority(
                    "ROLE_" + ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER))
        )
        .get()
        .uri("/api/clients/name/Power")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .json(TestConstants.BCREG_NAMELOOKUP_OK_RESPONSE);
  }

  @Test
  @DisplayName("Look for name 'Jhon'")
  void shouldGetNoDataFromNameLookup() {

    bcRegistryStub
        .stubFor(
            post(urlPathEqualTo("/registry-search/api/v2/search/businesses"))
                .withRequestBody(equalToJson("""
                        {
                          "query": { "value": "Jhon","name": "Jhon" },
                          "categories":{ "status":["ACTIVE"] },
                          "rows": 100,
                          "start":0
                        }"""
                    )
                )
                .willReturn(okJson(TestConstants.ORGBOOK_INCORP_EMPTY))
        );

    client
        .mutateWith(csrf())
        .mutateWith(
            mockJwt()
                .jwt(jwt -> jwt.claims(
                    claims -> claims.putAll(TestConstants.getClaims("bceidbusiness"))))
                .authorities(new SimpleGrantedAuthority(
                    "ROLE_" + ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER))
        )
        .get()
        .uri("/api/clients/name/Jhon")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .json(TestConstants.BCREG_NAMELOOKUP_EMPTY);
  }

  @Test
  @DisplayName("check for individual conflicts")
  void shouldCheckIndividual() {

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/idAndLastName"))
                .withQueryParam("clientId", equalTo("123456"))
                .withQueryParam("lastName", equalTo("Doe"))
                .willReturn(okJson(TestConstants.LEGACY_OK))
        );

    client
        .mutateWith(csrf())
        .mutateWith(
            mockJwt()
                .jwt(jwt -> jwt.claims(
                    claims -> claims.putAll(TestConstants.getClaims("bceidbusiness"))))
                .authorities(new SimpleGrantedAuthority(
                    "ROLE_" + ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER))
        )
        .get()
        .uri("/api/clients/individual/{userId}?lastName=Doe", Map.of("userId", "123456"))
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.CONFLICT)
        .expectBody(String.class)
        .isEqualTo("Client already exists with the client number 00000002");
  }

  @Test
  @DisplayName("check for individual conflicts and get none")
  void shouldCheckIndividualWithoutConflict() {

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/idAndLastName"))
                .withQueryParam("clientId", equalTo("123456"))
                .withQueryParam("lastName", equalTo("Doe"))
                .willReturn(okJson("[]"))
        );

    client
        .mutateWith(csrf())
        .mutateWith(
            mockJwt()
                .jwt(jwt -> jwt.claims(
                    claims -> claims.putAll(TestConstants.getClaims("bceidbusiness"))))
                .authorities(new SimpleGrantedAuthority(
                    "ROLE_" + ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER))
        )
        .get()
        .uri("/api/clients/individual/{userId}?lastName=Doe", Map.of("userId", "123456"))
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .isEmpty();
  }

  @ParameterizedTest
  @MethodSource("related")
  @DisplayName("List related clients")
  void shouldListRelatedClients(String clientNumber, Map<String, Long> expected,
      String legacyResponse) {

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/clients/" + clientNumber + "/related-clients"))
                .willReturn(okJson(legacyResponse))
        );

    var bodyExpectation =
        client
            .mutateWith(csrf())
            .mutateWith(
                mockJwt()
                    .jwt(jwt -> jwt.claims(
                        claims -> claims.putAll(TestConstants.getClaims("idir"))))
                    .authorities(new SimpleGrantedAuthority(
                        "ROLE_" + ApplicationConstant.ROLE_EDITOR))
            )
            .get()
            .uri("/api/clients/details/{clientNumber}/related-clients",
                Map.of("clientNumber", clientNumber))
            .exchange()
            .expectStatus().isOk()
            .expectBody();

    if (expected.isEmpty()) {
      bodyExpectation.json("{}");
    } else {
      expected.forEach((key, size) -> {
        bodyExpectation
            .jsonPath("$." + key).isArray()
            .jsonPath("$." + key + ".length()").isEqualTo(size);
      });
    }

  }

  @ParameterizedTest
  @MethodSource("relatedSearch")
  @DisplayName("Search related clients")
  void shouldSearchAutocompleteRelatedClients(
      String clientNumber,
      String type,
      String value,
      Long expectedSize
  ) {

    String legacyResponse = expectedSize > 0 ? "[{\"clientNumber\":\"" + clientNumber + "\"}]" : "[]";

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/relation/" + clientNumber))
                .withQueryParam("value", equalTo(value))
                .willReturn(okJson(legacyResponse))
        );


    var bodyExpectation =
        client
            .mutateWith(csrf())
            .mutateWith(
                mockJwt()
                    .jwt(jwt -> jwt.claims(
                        claims -> claims.putAll(TestConstants.getClaims("idir"))))
                    .authorities(new SimpleGrantedAuthority(
                        "ROLE_" + ApplicationConstant.ROLE_EDITOR))
            )
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/api/clients/relation/{clientNumber}")
                    .queryParamIfPresent("type", Optional.ofNullable(type))
                    .queryParam("value", value)
                    .build(clientNumber)
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody();

    if (expectedSize == 0) {
      bodyExpectation.json("[]");
    } else {
      bodyExpectation
          .jsonPath("$").isArray()
          .jsonPath("$.length()").isEqualTo(expectedSize);
    }
  }

  private static Stream<Arguments> clientDetailing() {
    return
        Stream.of(
            Arguments.of(
                named("All worked fine", "AA0000001"),
                200, TestConstants.BCREG_DOC_REQ_RES,
                200, TestConstants.BCREG_DOC_DATA,
                200, TestConstants.BCREG_RESPONSE_OK,
                TestConstants.LEGACY_EMPTY
            ),
            Arguments.of(
                named("Duplicated with all good due to legacy", "AA0000001"),
                200, TestConstants.BCREG_DOC_REQ_RES,
                200, TestConstants.BCREG_DOC_DATA,
                409, TestConstants.BCREG_RESPONSE_DUP,
                TestConstants.LEGACY_OK
            ),
            Arguments.of(
                named("All good, even with some legacy data", "AA0000001"),
                200, TestConstants.BCREG_DOC_REQ_RES,
                200, TestConstants.BCREG_DOC_DATA,
                200, TestConstants.BCREG_RESPONSE_OK,
                TestConstants.LEGACY_OK.replace("0000001", "0000002")
            ),
            Arguments.of(
                named("Can't find it, but facet can", "AA0000001"),
                404, TestConstants.BCREG_NOK,
                404, TestConstants.BCREG_NOK,
                200, BcRegistryTestConstants.BCREG_RESPONSE_ANY,
                TestConstants.LEGACY_EMPTY
            ),
            Arguments.of(
                named("Can't find what you're looking, but facet saved you", "AA0000001"),
                200, TestConstants.BCREG_DOC_REQ_RES,
                404, TestConstants.BCREG_NOK,
                200, BcRegistryTestConstants.BCREG_RESPONSE_ANY,
                TestConstants.LEGACY_EMPTY
            ),
            Arguments.of(
                named("Bc registry key is wrong", "AA0000001"),
                200, TestConstants.BCREG_DOC_REQ_RES,
                401, TestConstants.BCREG_401,
                401, TestConstants.BCREG_RESPONSE_401,
                TestConstants.LEGACY_EMPTY
            ),
            Arguments.of(
                named("All keys went south", "AA0000001"),
                401, TestConstants.BCREG_401,
                401, TestConstants.BCREG_401,
                401, TestConstants.BCREG_RESPONSE_401,
                TestConstants.LEGACY_EMPTY
            ),
            Arguments.of(
                named("Errors and keys", "AA0000001"),
                400, TestConstants.BCREG_400,
                400, TestConstants.BCREG_400,
                401, TestConstants.BCREG_RESPONSE_401,
                TestConstants.LEGACY_EMPTY
            ),

            Arguments.of(
                named("Sole prop from org is a no bueno", "AA0000001"),
                200, TestConstants.BCREG_DOC_REQ_RES,
                200, TestConstants.BCREG_DOC_DATA
                    .replace("\"partyType\": \"person\"", "\"partyType\": \"organization\"")
                    .replace("\"firstName\": \"JAMES\",", "\"organizationName\": \"OWNER ORG\",")
                    .replace("\"lastName\": \"BAXTER\",", "\"identifier\": \"BB0000001\",")
                    .replace("\"middleInitial\": \"middleInitial\",", "\"id\": \"1234467\",")
                ,
                422, "Unable to process request. This sole proprietor is not owner by a person",
                TestConstants.LEGACY_EMPTY
            )
        );
  }

  private static Stream<Arguments> clientDetailingStaff() {
    return
        Stream.of(
            Arguments.of(
                named("Bc registry ok and not on legacy", "AA0000001"),
                201, TestConstants.BCREG_DOC_REQ_RES,
                200, TestConstants.BCREG_DOC_DATA,
                TestConstants.LEGACY_EMPTY,
                200, TestConstants.BCREG_RESPONSE_OK
            ),
            Arguments.of(
                named("Bc registry ok and found on legacy", "AA0000001"),
                201, TestConstants.BCREG_DOC_REQ_RES,
                200, TestConstants.BCREG_DOC_DATA,
                TestConstants.LEGACY_OK,
                409, TestConstants.BCREG_RESPONSE_DUP
            ),
            Arguments.of(
                named("Bc registry ok and legacy with other ids", "AA0000001"),
                201, TestConstants.BCREG_DOC_REQ_RES,
                200, TestConstants.BCREG_DOC_DATA,
                TestConstants.LEGACY_OK.replace("0000001", "0000002"),
                200, TestConstants.BCREG_RESPONSE_OK
            ),
            Arguments.of(
                named("not found on bc registry", "AA0000001"),
                404, TestConstants.BCREG_NOK,
                404, TestConstants.BCREG_NOK,
                TestConstants.LEGACY_EMPTY,
                200, BcRegistryTestConstants.BCREG_RESPONSE_ANY
            ),
            Arguments.of(
                named("Bc Registry ok with no doc and no legacy", "AA0000001"),
                201, TestConstants.BCREG_DOC_REQ_RES,
                404, TestConstants.BCREG_NOK,
                TestConstants.LEGACY_EMPTY,
                200, BcRegistryTestConstants.BCREG_RESPONSE_ANY
            ),
            Arguments.of(
                named("Key error on bc registry", "AA0000001"),
                201, TestConstants.BCREG_DOC_REQ_RES,
                401, TestConstants.BCREG_401,
                TestConstants.LEGACY_EMPTY,
                401, TestConstants.BCREG_RESPONSE_401
            ),
            Arguments.of(
                named("Full key error", "AA0000001"),
                401, TestConstants.BCREG_401,
                401, TestConstants.BCREG_401,
                TestConstants.LEGACY_EMPTY,
                401, TestConstants.BCREG_RESPONSE_401
            ),
            Arguments.of(
                named("400 on Bc Registry", "AA0000001"),
                400, TestConstants.BCREG_400,
                400, TestConstants.BCREG_400,
                TestConstants.LEGACY_EMPTY,
                401, TestConstants.BCREG_RESPONSE_401
            ),
            Arguments.of(
                named("Bc Registry with no doc but ok on request", "AA0000001"),
                201, TestConstants.BCREG_DOC_REQ_RES,
                400, TestConstants.BCREG_400,
                TestConstants.LEGACY_EMPTY,
                401, TestConstants.BCREG_RESPONSE_401
            ),
            Arguments.of(
                named("Bc Registry with owner org", "AA0000001"),
                201, TestConstants.BCREG_DOC_REQ_RES,
                200, TestConstants.BCREG_DOC_DATA
                    .replace("\"partyType\": \"person\"", "\"partyType\": \"organization\"")
                    .replace("\"firstName\": \"JAMES\",", "\"organizationName\": \"OWNER ORG\",")
                    .replace("\"lastName\": \"BAXTER\",", "\"identifier\": \"BB0000001\",")
                    .replace("\"middleInitial\": \"middleInitial\",", "\"id\": \"1234467\",")
                ,
                TestConstants.LEGACY_EMPTY,
                200, TestConstants.BCREG_RESPONSE_NOCONTACTOK
            ),
            Arguments.of(
                named("BC Registry legal type change", "AA0000001"),
                201, TestConstants.BCREG_DOC_REQ_RES,
                200, TestConstants.BCREG_DOC_DATA
                    .replace("\"legalType\": \"SP\"", "\"legalType\": \"LP\""),
                TestConstants.LEGACY_EMPTY,
                200, TestConstants.BCREG_RESPONSE_OK
            )
        );
  }

  private static Stream<Arguments> related() {
    return
        Stream.of(
            argumentSet("No Related clients",
                "00000001",
                Map.of(),
                "[]"
            ),
            argumentSet(
                "Single entry with single location",
                "00000002",
                Map.of("00", 1L),
                "[{\"relatedClntLocn\": \"00\",\"primaryClient\": false}]"
            ),
            argumentSet(
                "Double location",
                "00000003",
                Map.of("00", 1L, "01", 1L),
                "[{\"relatedClntLocn\": \"00\",\"primaryClient\": false},{\"clientLocnCode\": \"01\",\"primaryClient\": true}]"
            )
        );
  }

  private static Stream<Arguments> relatedSearch() {
    return
        Stream.of(
            argumentSet(
                "Search by group, with no type for OAK HERITAGE GROUP",
                "00000158",
                null,
                "group",
                1L
            ),
            argumentSet(
                "James Hunt looking for other James",
                "00000009",
                null,
                "james",
                1L
            ),
            argumentSet(
                "James Hunt looking for other FM James",
                "00000009",
                "FM",
                "james",
                1L
            ),
            argumentSet(
                "James Hunt looking for other FM James with no results",
                "00000009",
                "FM",
                "james hunt hunt",
                0L
            )
        );
  }
  
  @ParameterizedTest(name = "{0}")
  @MethodSource("clientDetailsByIdProvider")
  @DisplayName("Client details by identification")
  void shouldGetClientDetailsByIdentification(
      String identification,
      int legacyStatus,
      String expectedResponse,
      int responseStatus) {

    reset();

    legacyStub.stubFor(
        get(urlPathEqualTo("/api/search/registrationOrName"))
            .withQueryParam("registrationNumber", equalTo(identification))
            .willReturn(
                status(legacyStatus)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(expectedResponse)));

    WebTestClient.BodyContentSpec response =
        client
            .mutateWith(csrf())
            .mutateWith(
                mockJwt()
                    .jwt(jwt -> jwt.claims(
                        claims -> claims.putAll(TestConstants.getClaims("bceidbusiness"))))
                    .authorities(
                        new SimpleGrantedAuthority(
                            "ROLE_" + ApplicationConstant.USERTYPE_BCEIDBUSINESS_USER)))
            .get()
            .uri(
                "/api/clients/details-by-id/{identification}",
                Map.of("identification", identification))
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.valueOf(responseStatus))
            .expectBody()
            .consumeWith(System.out::println);

    if (HttpStatus.valueOf(responseStatus).is2xxSuccessful()) {
      response.json(expectedResponse);
    } else {
      response.equals(expectedResponse);
    }
  }

  private static Stream<Arguments> clientDetailsByIdProvider() {
    String sampleClientJson = """
        [
          {
            "clientNumber": "AA0000001",
            "clientName": "SAMPLE COMPANY",
            "legalFirstName": null,
            "legalMiddleName": null,
            "clientStatusCode": "A",
            "clientTypeCode": "C",
            "clientIdTypeCode": null,
            "clientIdentification": "AA0000001",
            "registryCompanyTypeCode": "BC",
            "corpRegnNmbr": "C0123456",
            "clientAcronym": "SC",
            "wcbFirmNumber": null,
            "ocgSupplierNmbr": null,
            "clientComment": "Legacy client"
          }
        ]
        """;

    return Stream.of(
        Arguments.of("AA0000001", 200, sampleClientJson, 200),
        Arguments.of("INVALID123", 200, "[]", 200)
    );
  }

}
