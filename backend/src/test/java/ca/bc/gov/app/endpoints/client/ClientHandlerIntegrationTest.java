package ca.bc.gov.app.endpoints.client;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientBusinessTypeDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientInformationDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@DisplayName("Integrated Test | FSA Client Controller")
class ClientHandlerIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension
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
        .jsonPath("$[0].code").isEqualTo("C")

        .jsonPath("$[1].code").isNotEmpty()
        .jsonPath("$[1].code").isEqualTo("Z");

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

  @Test
  @DisplayName("Submit client data")
  void shouldSubmitClientData() {
    ClientSubmissionDto clientSubmissionDto =
        new ClientSubmissionDto(
            new ClientBusinessTypeDto("A"),
            new ClientInformationDto(
                "James", "Bond", "2007-07-07",
                "12345", "MI6", "Espionage"),
            new ClientLocationDto(
                List.of(
                    new ClientAddressDto(
                        "3570 S Las Vegas Blvd",
                        "US",
                        "NV",
                        "Las Vegas",
                        "89109",
                        "007",
                        0,
                        List.of(
                            new ClientContactDto(
                                "LP",
                                "James",
                                "Doe",
                                "007",
                                "bond_james_bond@007.com",
                                0))
                    )))
        );
    client
        .post()
        .uri("/api/clients/submissions")
        .body(Mono.just(clientSubmissionDto), ClientSubmissionDto.class)
        .exchange()
        .expectStatus().isCreated()
        .expectHeader().location("/api/clients/submissions/1")
        .expectHeader().valueEquals("x-sub-id", 1)
        .expectBody().isEmpty();
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
      String responseContent
  ) {

    wireMockExtension.resetAll();


    wireMockExtension
        .stubFor(get(urlPathEqualTo("/business/api/v2/businesses/" + clientNumber))
            .withHeader("x-apikey", equalTo("abc1234"))
            .withHeader("Account-Id", equalTo("account 0000"))
            .willReturn(
                status(detailsStatus)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(detailsResponse)
            )
        );

    wireMockExtension
        .stubFor(get(urlPathEqualTo("/business/api/v2/businesses/" + clientNumber + "/addresses"))
            .withHeader("x-apikey", equalTo("abc1234"))
            .withHeader("Account-Id", equalTo("account 0000"))
            .willReturn(
                status(addressStatus)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(addressResponse)
            )
        );


    WebTestClient.BodyContentSpec response =
        client
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

  private static Stream<Arguments> clientDetailing() {
    return
        Stream.of(
            Arguments.of(
                "AA0000001",
                200, TestConstants.BCREG_DETAIL_OK,
                200, TestConstants.BCREG_ADDR_OK,
                200, TestConstants.BCREG_RESPONSE_OK
            ),
            Arguments.of(
                "AA0000001",
                404, TestConstants.BCREG_NOK,
                404, TestConstants.BCREG_NOK,
                404, TestConstants.BCREG_RESPONSE_NOK
            ),
            Arguments.of(
                "AA0000001",
                200, TestConstants.BCREG_DETAIL_OK,
                404, TestConstants.BCREG_NOK,
                200, TestConstants.BCREG_RESPONSE_OK2
            ),
            Arguments.of(
                "AA0000001",
                200, TestConstants.BCREG_DETAIL_OK,
                401, TestConstants.BCREG_401,
                401, TestConstants.BCREG_RESPONSE_401
            ),
            Arguments.of(
                "AA0000001",
                401, TestConstants.BCREG_401,
                401, TestConstants.BCREG_401,
                401, TestConstants.BCREG_RESPONSE_401
            ),
            Arguments.of(
                "AA0000001",
                400, TestConstants.BCREG_400,
                400, TestConstants.BCREG_400,
                401, TestConstants.BCREG_RESPONSE_401
            ),

            Arguments.of(
                "AA0000001",
                200, TestConstants.BCREG_DETAIL_OK,
                400, TestConstants.BCREG_400,
                401, TestConstants.BCREG_RESPONSE_401
            )
        );
  }

  private static Stream<Arguments> countryCode() {
    return
        Stream.of(
            Arguments.of(null, null, "CA", "Canada"),
            Arguments.of(0, 1, "CA", "Canada"),
            Arguments.of(1, 1, "US", "United States of America"),
            Arguments.of(7, null, "BN", "Brunei Darussalam"),
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
