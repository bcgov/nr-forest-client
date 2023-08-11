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
import ca.bc.gov.app.dto.client.EmailRequestDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
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
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

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

  @Test
  @DisplayName("Codes are in expected order")
  void shouldListCodesAsExpected() {

    client
        .get()
        .uri("/api/clients/activeClientTypeCodes")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].code").isNotEmpty()
        .jsonPath("$[0].code").isEqualTo("A")

        .jsonPath("$[1].code").isNotEmpty()
        .jsonPath("$[1].code").isEqualTo("C");

  }

  @ParameterizedTest(name = "{2} - {3} is the first on page {0} with size {1}")
  @MethodSource("countryCode")
  @DisplayName("List countries by")
  void shouldListCountryData(Integer page, Integer size, String code, String name) {

    //This is to allow parameter to be ommitted during test
    Function<UriBuilder, URI> uri = uriBuilder -> {

      UriBuilder localBuilder = uriBuilder
          .path("/api/clients/activeCountryCodes");

      if (page != null) {
        localBuilder = localBuilder.queryParam("page", page);
      }
      if (size != null) {
        localBuilder = localBuilder.queryParam("size", size);
      }

      return localBuilder.build(new HashMap<>());
    };

    client
        .get()
        .uri(uri)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].code").isNotEmpty()
        .jsonPath("$[0].code").isEqualTo(code)
        .jsonPath("$[0].name").isNotEmpty()
        .jsonPath("$[0].name").isEqualTo(name);
  }


  @ParameterizedTest(name = "{3} - {4} is the first on page {1} with size {2} for country {0}")
  @MethodSource("provinceCode")
  @DisplayName("List provinces by")
  void shouldListProvinceData(String countryCode, Integer page, Integer size, String code,
                              String name) {

    //This is to allow parameter to be ommitted during test
    Function<UriBuilder, URI> uri = uriBuilder -> {

      UriBuilder localBuilder = uriBuilder
          .path("/api/clients/activeCountryCodes/{countryCode}");

      if (page != null) {
        localBuilder = localBuilder.queryParam("page", page);
      }
      if (size != null) {
        localBuilder = localBuilder.queryParam("size", size);
      }

      return localBuilder.build(Map.of("countryCode", countryCode));
    };

    client
        .get()
        .uri(uri)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].code").isNotEmpty()
        .jsonPath("$[0].code").isEqualTo(code)
        .jsonPath("$[0].name").isNotEmpty()
        .jsonPath("$[0].name").isEqualTo(name);
  }

  @ParameterizedTest(name = "{2} - {3} is the first on page {0} with size {1}")
  @MethodSource("contactTypeCodes")
  @DisplayName("List contact type codes")
  void shouldListContactTypes(Integer page, Integer size, String code, String description) {
    Function<UriBuilder, URI> uri = uriBuilder -> {

      UriBuilder localBuilder = uriBuilder
          .path("/api/clients/activeContactTypeCodes");

      if (page != null) {
        localBuilder = localBuilder.queryParam("page", page);
      }
      if (size != null) {
        localBuilder = localBuilder.queryParam("size", size);
      }

      return localBuilder.build(new HashMap<>());
    };

    client
        .get()
        .uri(uri)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].code").isNotEmpty()
        .jsonPath("$[0].code").isEqualTo(code)
        .jsonPath("$[0].name").isNotEmpty()
        .jsonPath("$[0].name").isEqualTo(description);
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
            .header(ApplicationConstant.USERID_HEADER,"testUserId")
            .header(ApplicationConstant.USERMAIL_HEADER,"test@test.ca")
            .header(ApplicationConstant.USERNAME_HEADER,"Test User")
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
        .header(ApplicationConstant.USERID_HEADER,"testUserId")
        .header(ApplicationConstant.USERMAIL_HEADER,"test@test.ca")
        .header(ApplicationConstant.USERNAME_HEADER,"Test User")
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
        .header(ApplicationConstant.USERID_HEADER,"testUserId")
        .header(ApplicationConstant.USERMAIL_HEADER,"test@test.ca")
        .header(ApplicationConstant.USERNAME_HEADER,"Test User")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .json(TestConstants.BCREG_NAMELOOKUP_EMPTY);
  }

  @Test
  @DisplayName("Send an email for already existing client")
  void shouldSendEmail(){
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

    ///search/incorporationOrName?incorporationNumber=XX1234567&companyName=Example%20Inc.
    legacyStub
        .stubFor(
            get(urlPathEqualTo("/search/incorporationOrName"))
                .withQueryParam("incorporationNumber", equalTo(TestConstants.EMAIL_REQUEST.incorporation()))
                .withQueryParam("companyName", equalTo(TestConstants.EMAIL_REQUEST.name()))
                .willReturn(okJson(TestConstants.LEGACY_OK))
        );

    client
        .post()
        .uri("/api/clients/mail")
        .body(Mono.just(TestConstants.EMAIL_REQUEST), EmailRequestDto.class)
        .exchange()
        .expectStatus().isAccepted()
        .expectBody().isEmpty();

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
                TestConstants.LEGACY_OK.replace("0000001","0000002")
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
            )
        );
  }

  private static Stream<Arguments> countryCode() {
    return
        Stream.of(
            Arguments.of(null, null, "CA", "Canada"),
            Arguments.of(0, 1, "CA", "Canada"),
            Arguments.of(1, 1, "US", "United States of America"),
            Arguments.of(7, null, "EE", "Estonia"),
            Arguments.of(3, 10, "BA", "Bosnia and Herzegovina"),
            Arguments.of(33, 1, "BR", "Brazil"),
            Arguments.of(49, 1, "CO", "Colombia")
        );
  }

  private static Stream<Arguments> provinceCode() {
    return
        Stream.of(
            Arguments.of("CA", null, null, "AB", "Alberta"),
            Arguments.of("CA", 0, 1, "AB", "Alberta"),
            Arguments.of("US", 1, 1, "AK", "Alaska"));
  }

  private static Stream<Arguments> contactTypeCodes() {
    return
        Stream.of(
            Arguments.of(null, null, "AP", "Accounts Payable"),
            Arguments.of(0, 1, "AP", "Accounts Payable"),
            Arguments.of(1, 1, "AR", "Accounts Receivable"),
            Arguments.of(11, 1, "GP", "General Partner"),
            Arguments.of(22, 1, "TP", "EDI Trading Partner")
        );
  }

}
