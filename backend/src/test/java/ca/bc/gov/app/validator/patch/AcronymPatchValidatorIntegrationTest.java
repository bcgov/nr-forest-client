package ca.bc.gov.app.validator.patch;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import ca.bc.gov.app.exception.ValidationException;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

@DisplayName("Integrated Test | Patch Validator : Acronym")
class AcronymPatchValidatorIntegrationTest extends AbstractTestContainerIntegrationTest {

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
  private AcronymPatchValidator validator;

  private static final JsonNode NODE = new ObjectMapper().createObjectNode()
      .put("op", "replace")
      .put("path", "/client/clientAcronym")
      .put("value", "ABC");


  @ParameterizedTest
  @MethodSource("validationAllowed")
  @DisplayName("Should check if validation is allowed")
  void shouldCheckIfValidationAllowed(JsonNode node, boolean valid) {
    Assertions.assertEquals(valid, validator.shouldValidate().test(node));
  }

  @Test
  @DisplayName("Should proceed with no issues")
  void shouldProceedWithNoIssues() {

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/acronym"))
                .withQueryParam("acronym", equalTo("ABC"))
                .willReturn(okJson("[]"))
        );

    StepVerifier
        .create(validator.validate("00000001").apply(NODE))
        .expectNext(NODE)
        .verifyComplete();
  }

  @Test
  @DisplayName("Should fail with size issue")
  void shouldFailWithSizeIssue() {

    StepVerifier
        .create(validator.validate("00000001").apply(
            new ObjectMapper().createObjectNode()
                .put("op", "replace")
                .put("path", "/client/clientAcronym")
                .put("value", "W")
        ))
        .expectError(ValidationException.class)
        .verify();

  }

  @Test
  @DisplayName("Should fail with uniqueness issue")
  void shouldFailWithUniquenessIssue() {
    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/acronym"))
                .withQueryParam("acronym", equalTo("ABC"))
                .willReturn(okJson("[{\"clientNumber\":\"00000001\"}]"))
        );

    StepVerifier
        .create(validator.validate("00000001").apply(NODE))
        .expectError(ValidationException.class)
        .verify();
  }

  private static Stream<Arguments> validationAllowed() {
    return Stream
        .of(
            Arguments.of(NODE, true),
            Arguments.of(new ObjectMapper().createObjectNode()
                .put("op", "add")
                .put("path", "/client/clientName")
                .put("value", "ABC"), false)
        );
  }
}