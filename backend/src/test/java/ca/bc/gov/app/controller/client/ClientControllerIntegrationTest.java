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

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@DisplayName("Integrated Test | FSA Client Controller")
class ClientControllerIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

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

  @ParameterizedTest
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
            get(urlPathEqualTo("/search/incorporationOrName"))
                .withQueryParam("incorporationNumber", equalTo("AA0000001"))
                .withQueryParam("companyName", equalTo("SAMPLE COMPANY"))
                .willReturn(okJson(legacyResponse))
        );

    WebTestClient.BodyContentSpec response =
        client
            .get()
            .uri("/api/clients/{clientNumber}", Map.of("clientNumber", clientNumber))
            .header(ApplicationConstant.USERID_HEADER, "testUserId")
            .header(ApplicationConstant.USERMAIL_HEADER, "test@test.ca")
            .header(ApplicationConstant.USERNAME_HEADER, "Test User")
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
            get(urlPathEqualTo("/registry-search/api/v1/businesses/search/facets"))
                .withQueryParam("category", equalTo("status:Active"))
                .withQueryParam("start", equalTo("0"))
                .withQueryParam("rows", equalTo("100"))
                .withQueryParam("query", equalTo("value:BC0772006"))
                .willReturn(okJson(TestConstants.ORGBOOK_INCORP_OK))
        );

    client
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
            get(urlPathEqualTo("/registry-search/api/v1/businesses/search/facets"))
                .withQueryParam("category", equalTo("status:Active"))
                .withQueryParam("start", equalTo("0"))
                .withQueryParam("rows", equalTo("100"))
                .withQueryParam("query", equalTo("value:BC0000000"))
                .willReturn(okJson(TestConstants.ORGBOOK_INCORP_EMPTY))
        );

    client
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
            get(urlPathEqualTo("/registry-search/api/v1/businesses/search/facets"))
                .withQueryParam("category", equalTo("status:Active"))
                .withQueryParam("start", equalTo("0"))
                .withQueryParam("rows", equalTo("100"))
                .withQueryParam("query", equalTo("value:Power"))
                .willReturn(okJson(TestConstants.ORGBOOK_INCORP_OK))
        );

    client
        .get()
        .uri("/api/clients/name/Power")
        .header(ApplicationConstant.USERID_HEADER, "testUserId")
        .header(ApplicationConstant.USERMAIL_HEADER, "test@test.ca")
        .header(ApplicationConstant.USERNAME_HEADER, "Test User")
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
            get(urlPathEqualTo("/registry-search/api/v1/businesses/search/facets"))
                .withQueryParam("category", equalTo("status:Active"))
                .withQueryParam("start", equalTo("0"))
                .withQueryParam("rows", equalTo("100"))
                .withQueryParam("query", equalTo("value:Jhon"))
                .willReturn(okJson(TestConstants.ORGBOOK_INCORP_EMPTY))
        );

    client
        .get()
        .uri("/api/clients/name/Jhon")
        .header(ApplicationConstant.USERID_HEADER, "testUserId")
        .header(ApplicationConstant.USERMAIL_HEADER, "test@test.ca")
        .header(ApplicationConstant.USERNAME_HEADER, "Test User")
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
            get(urlPathEqualTo("/search/idAndLastName"))
                .withQueryParam("clientId", equalTo("123456"))
                .withQueryParam("lastName", equalTo("Doe"))
                .willReturn(okJson(TestConstants.LEGACY_OK))
        );

    client
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
            get(urlPathEqualTo("/search/idAndLastName"))
                .withQueryParam("clientId", equalTo("123456"))
                .withQueryParam("lastName", equalTo("Doe"))
                .willReturn(okJson("[]"))
        );

    client
        .get()
        .uri("/api/clients/individual/{userId}?lastName=Doe", Map.of("userId", "123456"))
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .isEmpty();
  }

  private static Stream<Arguments> clientDetailing() {
    return
        Stream.of(
            Arguments.of(
                "AA0000001",
                200, TestConstants.BCREG_DOC_REQ_RES,
                200, TestConstants.BCREG_DOC_DATA,
                200, TestConstants.BCREG_RESPONSE_OK,
                TestConstants.LEGACY_EMPTY
            ),
            Arguments.of(
                "AA0000001",
                200, TestConstants.BCREG_DOC_REQ_RES,
                200, TestConstants.BCREG_DOC_DATA,
                409, TestConstants.BCREG_RESPONSE_DUP,
                TestConstants.LEGACY_OK
            ),
            Arguments.of(
                "AA0000001",
                200, TestConstants.BCREG_DOC_REQ_RES,
                200, TestConstants.BCREG_DOC_DATA,
                200, TestConstants.BCREG_RESPONSE_OK,
                TestConstants.LEGACY_OK.replace("0000001", "0000002")
            ),
            Arguments.of(
                "AA0000001",
                404, TestConstants.BCREG_NOK,
                404, TestConstants.BCREG_NOK,
                404, TestConstants.BCREG_RESPONSE_NOK,
                TestConstants.LEGACY_EMPTY
            ),
            Arguments.of(
                "AA0000001",
                200, TestConstants.BCREG_DOC_REQ_RES,
                404, TestConstants.BCREG_NOK,
                404, TestConstants.BCREG_RESPONSE_NOK,
                TestConstants.LEGACY_EMPTY
            ),
            Arguments.of(
                "AA0000001",
                200, TestConstants.BCREG_DOC_REQ_RES,
                401, TestConstants.BCREG_401,
                401, TestConstants.BCREG_RESPONSE_401,
                TestConstants.LEGACY_EMPTY
            ),
            Arguments.of(
                "AA0000001",
                401, TestConstants.BCREG_401,
                401, TestConstants.BCREG_401,
                401, TestConstants.BCREG_RESPONSE_401,
                TestConstants.LEGACY_EMPTY
            ),
            Arguments.of(
                "AA0000001",
                400, TestConstants.BCREG_400,
                400, TestConstants.BCREG_400,
                401, TestConstants.BCREG_RESPONSE_401,
                TestConstants.LEGACY_EMPTY
            ),

            Arguments.of(
                "AA0000001",
                200, TestConstants.BCREG_DOC_REQ_RES,
                400, TestConstants.BCREG_400,
                401, TestConstants.BCREG_RESPONSE_401,
                TestConstants.LEGACY_EMPTY
            ),
            Arguments.of(
                "AA0000001",
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

}
