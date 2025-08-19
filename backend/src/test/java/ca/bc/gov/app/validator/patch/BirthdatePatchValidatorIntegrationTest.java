package ca.bc.gov.app.validator.patch;

import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

@DisplayName("Integrated Test | Patch Validator : Birthdate")
class BirthdatePatchValidatorIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  private BirthdatePatchValidator validator;

  public static final ObjectMapper MAPPER = new ObjectMapper();
  private static final JsonNode NODE = MAPPER.createObjectNode()
      .put("op", "replace")
      .put("path", "/client/birthdate")
      .put("value", "1971-02-03");

  @ParameterizedTest
  @MethodSource("validationAllowed")
  @DisplayName("Should check if validation is allowed")
  void shouldCheckIfValidationAllowed(JsonNode node, boolean valid) {
    Assertions.assertEquals(valid, validator.shouldValidate().test(node));
  }

  @Test
  @DisplayName("Should proceed with no issues")
  void shouldProceedWithNoIssues() {

    StepVerifier
        .create(validator.validate("00000001").apply(NODE))
        .expectNext(MAPPER.createObjectNode()
            .put("op", "replace")
            .put("path", "/client/birthdate")
            .put("value", "1971-02-03 00:00:00"))
        .verifyComplete();
  }

  @Test
  @DisplayName("Should proceed with no issues when empty")
  void shouldProceedWithNoIssuesWhenEmpty() {

    StepVerifier
        .create(validator.validate("00000001").apply(
            MAPPER.createObjectNode()
                .put("op", "replace")
                .put("path", "/client/birthdate")
                .put("value", StringUtils.EMPTY)
        ))
        .expectNext(MAPPER.createObjectNode()
            .put("op", "replace")
            .put("path", "/client/birthdate")
            .put("value", StringUtils.EMPTY))
        .verifyComplete();
  }

  private static Stream<Arguments> validationAllowed() {
    return Stream
        .of(
            Arguments.of(NODE, true),
            Arguments.of(MAPPER.createObjectNode()
                .put("op", "add")
                .put("path", "/client/clientName")
                .put("value", "ABC"), false),
            Arguments.of(MAPPER.createObjectNode()
                .put("op", "add")
                .put("path", "/client/clientName")
                .put("value", StringUtils.EMPTY), false)
        );
  }

}