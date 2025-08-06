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

@DisplayName("Integrated Test | Patch Validator : Registration number")
class RegistrationNumberPatchValidatorIntegrationTest extends AbstractTestContainerIntegrationTest {

  public static final String CLIENT_NUMBER = "01000001";
  
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
  private RegistrationNumberPatchValidator validator;

  public static final ObjectMapper MAPPER = new ObjectMapper();

  private static final JsonNode ID = MAPPER.createObjectNode()
      .put("op", "replace")
      .put("path", "/client/registryCompanyTypeCode")
      .put("value", "FM");

  private static final JsonNode NUMBER = MAPPER.createObjectNode()
      .put("op", "replace")
      .put("path", "/client/corpRegnNmbr")
      .put("value", "00000099");

  @ParameterizedTest
  @MethodSource("validationAllowed")
  @DisplayName("Should check if validation is allowed")
  void shouldCheckIfValidationAllowed(JsonNode node, boolean valid) {
    Assertions.assertEquals(valid, validator.shouldValidate().test(node));
  }

  @Test
  @DisplayName("Validation is always allowed")
  void shouldNotValidateAsNoCodeExist() {
    validator
        .validate("00000001")
        .apply(ID)
        .as(StepVerifier::create)
        .expectNext(ID)
        .verifyComplete();

  }

  @Test
  @DisplayName("Should proceed with no issues")
  void shouldProceedWithNoIssues() {

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/corporationValues/" + CLIENT_NUMBER))
                .withQueryParam("corpRegnNmbr", equalTo("00000099"))
                .willReturn(okJson("[]"))
        );

    validator
        .globalValidator(asArray(NUMBER), CLIENT_NUMBER)
        .apply(NUMBER)
        .as(StepVerifier::create)
        .expectNext(NUMBER)
        .verifyComplete();
  }

  @Test
  @DisplayName("Should fail with uniqueness issue")
  void shouldFailWithUniquenessIssue() {
    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/corporationValues/" + CLIENT_NUMBER))
                .withQueryParam("registryCompanyTypeCode", equalTo("FM"))
                .willReturn(okJson("[{\"clientNumber\":\"00000001\"}]"))
        );

    validator
        .globalValidator(asArray(ID), CLIENT_NUMBER)
        .apply(ID)
        .as(StepVerifier::create)
        .expectError(ValidationException.class)
        .verify();
  }

  private static Stream<Arguments> validationAllowed() {
    return Stream
        .of(
            Arguments.of(ID, true),
            Arguments.of(NUMBER, true),
            Arguments.of(new ObjectMapper().createObjectNode()
                .put("op", "add")
                .put("path", "/client/clientName")
                .put("value", "ABC"), false)
        );
  }

  private static JsonNode asArray(JsonNode... nodes) {
    return MAPPER.createArrayNode().addAll(Stream.of(nodes).toList());
  }

}
