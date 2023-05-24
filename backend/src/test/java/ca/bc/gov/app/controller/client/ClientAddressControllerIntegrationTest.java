package ca.bc.gov.app.controller.client;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import ca.bc.gov.app.AddressTestConstants;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@DisplayName("Integrated Test | FSA Client Address Controller")
class ClientAddressControllerIntegrationTest  extends AbstractTestContainerIntegrationTest {
  @Autowired
  protected WebTestClient client;

  private static final String FIND_URI = "/find/v2.10/json3.ws";
  private static final String RETRIEVE_URI = "/retrieve/v2.11/json3.ws";

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10050)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();

  @BeforeEach
  public void reset() {
    wireMockExtension.resetAll();

    client = client.mutate()
        .responseTimeout(Duration.ofSeconds(10))
        .build();
  }

  @Test
  @DisplayName("Return error possible addresses")
  void shouldReturnErrorPossibleAddresses() {
    wireMockExtension
        .stubFor(
            get(urlPathEqualTo(FIND_URI))
                .withQueryParam("key", equalTo(AddressTestConstants.ADDRESS_API_KEY))
                .withQueryParam("Country", equalTo("BR"))
                .withQueryParam("MaxSuggestions", equalTo("7"))
                .withQueryParam("SearchTerm", equalTo(""))
                .willReturn(okJson(AddressTestConstants.ERROR))
        );

    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/clients/addresses")
                .queryParam("country", "BR")
                .queryParam("searchTerm", "")
                .build(Map.of()))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  @DisplayName("Return empty possible addresses")
  void shouldReturnEmptyPossibleAddresses() {
    wireMockExtension
        .stubFor(
            get(urlPathEqualTo(FIND_URI))
                .withQueryParam("key", equalTo(AddressTestConstants.ADDRESS_API_KEY))
                .withQueryParam("Country", equalTo("BR"))
                .withQueryParam("MaxSuggestions", equalTo("7"))
                .withQueryParam("SearchTerm", equalTo("rua oito jp"))
                .willReturn(okJson(AddressTestConstants.ONLY_FIND_ADDRESS))
        );

    wireMockExtension
        .stubFor(
            get(urlPathEqualTo(FIND_URI))
                .withQueryParam("key", equalTo(AddressTestConstants.ADDRESS_API_KEY))
                .withQueryParam("LastId", equalTo(AddressTestConstants.LAST_ID))
                .willReturn(okJson(AddressTestConstants.EMPTY))
        );

    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/clients/addresses")
                .queryParam("country", "BR")
                .queryParam("searchTerm", "rua oito jp")
                .build(Map.of()))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  @DisplayName("Find possible addresses after a find is needed")
  void shouldReturnPossibleAddressesAfterFind() {
    wireMockExtension
        .stubFor(
            get(urlPathEqualTo(FIND_URI))
                .withQueryParam("key", equalTo(AddressTestConstants.ADDRESS_API_KEY))
                .withQueryParam("Country", equalTo("BR"))
                .withQueryParam("MaxSuggestions", equalTo("7"))
                .withQueryParam("SearchTerm", equalTo("rua oito jp"))
                .willReturn(okJson(AddressTestConstants.ONLY_FIND_ADDRESS))
        );

    wireMockExtension
        .stubFor(
            get(urlPathEqualTo(FIND_URI))
                .withQueryParam("key", equalTo(AddressTestConstants.ADDRESS_API_KEY))
                .withQueryParam("LastId", equalTo(AddressTestConstants.LAST_ID))
                .willReturn(okJson(AddressTestConstants.POSSIBLE_ADDRESSES_AFTER_FIND))
        );

    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/clients/addresses")
                .queryParam("country", "BR")
                .queryParam("searchTerm", "rua oito jp")
                .build(Map.of()))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  @DisplayName("Find possible addresses")
  void shouldReturnPossibleAddresses() {
    wireMockExtension
        .stubFor(
            get(urlPathEqualTo(FIND_URI))
                .withQueryParam("key", equalTo(AddressTestConstants.ADDRESS_API_KEY))
                .withQueryParam("Country", equalTo("CA"))
                .withQueryParam("MaxSuggestions", equalTo("7"))
                .withQueryParam("SearchTerm", equalTo("511-860"))
                .willReturn(okJson(AddressTestConstants.POSSIBLE_ADDRESSES))
        );

    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/clients/addresses")
                .queryParam("searchTerm", "511-860")
                .build(Map.of()))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  @DisplayName("Get address by id error")
  void shouldReturnAddressError() {
    wireMockExtension
        .stubFor(
            get(urlPathEqualTo(RETRIEVE_URI))
                .withQueryParam("key", equalTo(AddressTestConstants.ADDRESS_API_KEY))
                .withQueryParam("id", equalTo("CA|CP|B|0000001"))
                .willReturn(okJson(AddressTestConstants.ERROR))
        );

    client
        .get()
        .uri("/api/clients/address/CA|CP|B|0000001")
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  @DisplayName("Get address by id")
  void shouldReturnAddress() {
    wireMockExtension
        .stubFor(
            get(urlPathEqualTo(RETRIEVE_URI))
                .withQueryParam("key", equalTo(AddressTestConstants.ADDRESS_API_KEY))
                .withQueryParam("id", equalTo("CA|CP|B|0000001"))
                .willReturn(okJson(AddressTestConstants.GET_ADDRESS))
        );

    client
        .get()
        .uri("/api/clients/address/CA|CP|B|0000001")
        .exchange()
        .expectStatus()
        .isOk();
  }
}
