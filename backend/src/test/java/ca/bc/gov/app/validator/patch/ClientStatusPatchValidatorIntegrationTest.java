package ca.bc.gov.app.validator.patch;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

@DisplayName("Integrated Test | Patch Validator : Client Status")
class ClientStatusPatchValidatorIntegrationTest extends AbstractTestContainerIntegrationTest {

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
  private ClientStatusPatchValidator validator;

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final JsonNode DEACTIVATE_STATUS = MAPPER
      .createObjectNode()
      .put("op", "replace")
      .put("path", "/client/clientStatusCode")
      .put("value", "DAC");

  private static final JsonNode ACTIVATE_STATUS = MAPPER
      .createObjectNode()
      .put("op", "replace")
      .put("path", "/client/clientStatusCode")
      .put("value", "ACT");

  private static final JsonNode OTHER_STATUS = MAPPER
      .createObjectNode()
      .put("op", "replace")
      .put("path", "/client/clientStatusCode")
      .put("value", "SPN");

  private static final JsonNode OTHER_FIELD = MAPPER
      .createObjectNode()
      .put("op", "replace")
      .put("path", "/client/clientName")
      .put("value", "SOME NAME");

  private static final JsonNode ADD_OP = MAPPER
      .createObjectNode()
      .put("op", "add")
      .put("path", "/client/clientStatusCode")
      .put("value", "DAC");

  @BeforeEach
  void setUp() {
    legacyStub.resetAll();
  }

  @ParameterizedTest
  @MethodSource("validationAllowed")
  @DisplayName("Should check if validation is allowed")
  void shouldCheckIfValidationAllowed(JsonNode node, boolean valid) {
    Assertions.assertEquals(valid, validator.shouldValidate().test(node));
  }

  @Test
  @DisplayName("Validate method should return input node unchanged")
  void shouldReturnInputNodeUnchanged() {
    StepVerifier
        .create(validator.validate("00000001").apply(DEACTIVATE_STATUS))
        .expectNext(DEACTIVATE_STATUS)
        .verifyComplete();
  }

  @Test
  @DisplayName("Should deactivate active locations when deactivating client")
  void shouldDeactivateActiveLocationsWhenDeactivatingClient() {
    String clientNumber = "00000001";

    legacyStub.stubFor(
        get(urlPathEqualTo("/api/search/clientNumber/" + clientNumber))
            .willReturn(okJson("""
                {
                  "client": {
                    "clientNumber": "00000001",
                    "clientName": "TEST CLIENT",
                    "clientStatusCode": "ACT"
                  },
                  "addresses": [
                    {
                      "clientNumber": "00000001",
                      "clientLocnCode": "00",
                      "clientLocnName": "Main Office",
                      "locnExpiredInd": "N"
                    },
                    {
                      "clientNumber": "00000001",
                      "clientLocnCode": "01",
                      "clientLocnName": "Branch Office",
                      "locnExpiredInd": "N"
                    },
                    {
                      "clientNumber": "00000001",
                      "clientLocnCode": "02",
                      "clientLocnName": "Expired Location",
                      "locnExpiredInd": "Y"
                    }
                  ],
                  "contacts": null,
                  "doingBusinessAs": null
                }
                """))
    );

    JsonNode globalForestClient = MAPPER.createArrayNode().add(DEACTIVATE_STATUS);

    StepVerifier
        .create(
            validator.globalValidator(globalForestClient, clientNumber).apply(DEACTIVATE_STATUS))
        .assertNext(result -> {
          assertTrue(result instanceof ArrayNode, "Result should be an ArrayNode");
          ArrayNode arrayResult = (ArrayNode) result;

          // Should contain original node + 2 location update nodes (excluding expired one)
          assertEquals(3, arrayResult.size());

          // Verify location update patches are created for active locations
          boolean has00LocationPatch = false;
          boolean has01LocationPatch = false;

          for (JsonNode node : arrayResult) {
            String path = node.get("path").asText();
            if (path.equals("/addresses/00/locnExpiredInd")) {
              has00LocationPatch = true;
              assertEquals("replace", node.get("op").asText());
              assertEquals("Y", node.get("value").asText());
            }
            if (path.equals("/addresses/01/locnExpiredInd")) {
              has01LocationPatch = true;
              assertEquals("replace", node.get("op").asText());
              assertEquals("Y", node.get("value").asText());
            }
          }

          assertTrue(has00LocationPatch, "Should have patch for location 00");
          assertTrue(has01LocationPatch, "Should have patch for location 01");
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("Should activate deactivated locations when activating client")
  void shouldActivateDeactivatedLocationsWhenActivatingClient() {
    String clientNumber = "00000001";

    legacyStub.stubFor(
        get(urlPathEqualTo("/api/locations/" + clientNumber + "/DAC"))
            .willReturn(okJson("""
                [
                  {"code": "00", "name": "Main Office"},
                  {"code": "01", "name": "Branch Office"}
                ]
                """))
    );

    JsonNode globalForestClient = MAPPER.createArrayNode().add(ACTIVATE_STATUS);

    StepVerifier
        .create(validator.globalValidator(globalForestClient, clientNumber).apply(ACTIVATE_STATUS))
        .assertNext(result -> {
          assertTrue(result instanceof ArrayNode, "Result should be an ArrayNode");
          ArrayNode arrayResult = (ArrayNode) result;

          // Should contain original node + 2 location update nodes
          assertEquals(3, arrayResult.size());

          // Verify location update patches are created for reactivation
          boolean has00LocationPatch = false;
          boolean has01LocationPatch = false;

          for (JsonNode node : arrayResult) {
            String path = node.get("path").asText();
            if (path.equals("/addresses/00/locnExpiredInd")) {
              has00LocationPatch = true;
              assertEquals("replace", node.get("op").asText());
              assertEquals("N", node.get("value").asText());
            }
            if (path.equals("/addresses/01/locnExpiredInd")) {
              has01LocationPatch = true;
              assertEquals("replace", node.get("op").asText());
              assertEquals("N", node.get("value").asText());
            }
          }

          assertTrue(has00LocationPatch, "Should have patch for location 00");
          assertTrue(has01LocationPatch, "Should have patch for location 01");
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("Should return original node when no active locations to deactivate")
  void shouldReturnOriginalNodeWhenNoActiveLocationsToDeactivate() {
    String clientNumber = "00000001";

    legacyStub.stubFor(
        get(urlPathEqualTo("/api/search/clientNumber/" + clientNumber))
            .willReturn(okJson("""
                {
                  "client": {
                    "clientNumber": "00000001",
                    "clientName": "TEST CLIENT",
                    "clientStatusCode": "ACT"
                  },
                  "addresses": [
                    {
                      "clientNumber": "00000001",
                      "clientLocnCode": "00",
                      "clientLocnName": "Expired Location",
                      "locnExpiredInd": "Y"
                    }
                  ],
                  "contacts": null,
                  "doingBusinessAs": null
                }
                """))
    );

    JsonNode globalForestClient = MAPPER.createArrayNode().add(DEACTIVATE_STATUS);

    StepVerifier
        .create(
            validator.globalValidator(globalForestClient, clientNumber).apply(DEACTIVATE_STATUS))
        .assertNext(result -> {
          // When no locations to update, original node should be returned
          assertEquals(DEACTIVATE_STATUS, result);
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("Should return original node when no locations to reactivate")
  void shouldReturnOriginalNodeWhenNoLocationsToReactivate() {
    String clientNumber = "00000001";

    legacyStub.stubFor(
        get(urlPathEqualTo("/api/locations/" + clientNumber + "/DAC"))
            .willReturn(okJson("[]"))
    );

    JsonNode globalForestClient = MAPPER.createArrayNode().add(ACTIVATE_STATUS);

    StepVerifier
        .create(validator.globalValidator(globalForestClient, clientNumber).apply(ACTIVATE_STATUS))
        .assertNext(result -> {
          // When no locations to update, original node should be returned
          assertEquals(ACTIVATE_STATUS, result);
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("Should handle null addresses when deactivating")
  void shouldHandleNullAddressesWhenDeactivating() {
    String clientNumber = "00000001";

    legacyStub.stubFor(
        get(urlPathEqualTo("/api/search/clientNumber/" + clientNumber))
            .willReturn(okJson("""
                {
                  "client": {
                    "clientNumber": "00000001",
                    "clientName": "TEST CLIENT",
                    "clientStatusCode": "ACT"
                  },
                  "addresses": null,
                  "contacts": null,
                  "doingBusinessAs": null
                }
                """))
    );

    JsonNode globalForestClient = MAPPER.createArrayNode().add(DEACTIVATE_STATUS);

    StepVerifier
        .create(
            validator.globalValidator(globalForestClient, clientNumber).apply(DEACTIVATE_STATUS))
        .assertNext(result -> {
          // When addresses is null, original node should be returned
          assertEquals(DEACTIVATE_STATUS, result);
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("Should create correct location update node format")
  void shouldCreateCorrectLocationUpdateNodeFormat() {
    JsonNode deactivateNode = validator.updateLocation("00", true);

    assertEquals("replace", deactivateNode.get("op").asText());
    assertEquals("/addresses/00/locnExpiredInd", deactivateNode.get("path").asText());
    assertEquals("Y", deactivateNode.get("value").asText());

    JsonNode activateNode = validator.updateLocation("01", false);

    assertEquals("replace", activateNode.get("op").asText());
    assertEquals("/addresses/01/locnExpiredInd", activateNode.get("path").asText());
    assertEquals("N", activateNode.get("value").asText());
  }

  private static Stream<Arguments> validationAllowed() {
    return Stream.of(
        Arguments.of(DEACTIVATE_STATUS, true),
        Arguments.of(ACTIVATE_STATUS, true),
        Arguments.of(OTHER_STATUS, false),
        Arguments.of(OTHER_FIELD, false),
        Arguments.of(ADD_OP, false)
    );
  }
}

