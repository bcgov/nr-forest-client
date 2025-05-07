package ca.bc.gov.app.service.client;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

@DisplayName("Integrated Test | Client Patch Service")
class ClientPatchServiceIntegrationTest extends AbstractTestContainerIntegrationTest {

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
  private ClientPatchService service;

  @Autowired
  private ObjectMapper objectMapper;

  private JsonNode node;

  @BeforeEach
  void setup() {
    legacyStub.resetAll();

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/acronym"))
                .willReturn(okJson("[]"))
        );

    legacyStub
        .stubFor(
            patch(urlPathEqualTo("/api/clients/partial/00000000"))
                .willReturn(okJson(""))
        );

    node = objectMapper.createArrayNode()
        .add(objectMapper.createObjectNode()
            .put("op", "replace")
            .put("path", "/client/clientAcronym")
            .put("value", "ABC")
        )
        .add(objectMapper.createObjectNode()
            .put("op", "replace")
            .put("path", "/client/clientOtherField")
            .put("value", "ABC")
        );
  }

  @Test
  void shouldSendPatchRequest() {

    service.patchClient("00000000", node, "testUser")
        .as(StepVerifier::create)
        .expectComplete()
        .verify();

    legacyStub.verify(1, patchRequestedFor(urlPathEqualTo("/api/clients/partial/00000000")));
    legacyStub.verify(1, getRequestedFor(urlPathEqualTo("/api/search/acronym"))
        .withQueryParam("acronym", equalTo("ABC")));

  }

}