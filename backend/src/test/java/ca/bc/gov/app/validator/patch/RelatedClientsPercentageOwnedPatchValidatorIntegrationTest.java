package ca.bc.gov.app.validator.patch;

import ca.bc.gov.app.exception.ValidationException;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

@DisplayName("Integrated Test | Patch Validator : Related Clients - Percentage Owned")
public class RelatedClientsPercentageOwnedPatchValidatorIntegrationTest
    extends AbstractTestContainerIntegrationTest {

  @Autowired
  private RelatedClientsPercentageOwnedPatchValidator validator;

  public static final String PATCH_PATH = "/relatedClients/00/0/percentageOwnership";
  public static final String CLIENT_NUMBER = "01000001";
  public static final ObjectMapper MAPPER = new ObjectMapper();

  private static final JsonNode NODE = MAPPER.createObjectNode()
      .put("op", "replace")
      .put("path", PATCH_PATH)
      .put("value", 55.00);

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
        .create(validator.validate(CLIENT_NUMBER).apply(NODE))
        .expectNext(NODE)
        .verifyComplete();
  }

  @Test
  @DisplayName("Should fail if percentage is out of bounds")
  void shouldFailIfPercentageOutOfBounds() {
    JsonNode node = MAPPER.createObjectNode()
        .put("op", "replace")
        .put("path", PATCH_PATH)
        .put("value", 150.00);

    StepVerifier
        .create(validator.validate(CLIENT_NUMBER).apply(node))
        .expectError(ValidationException.class)
        .verify();
  }

  @Test
  @DisplayName("Should fail if percentage is not a number")
  void shouldFailIfPercentageIsNotNumber() {
    JsonNode node = MAPPER.createObjectNode()
        .put("op", "replace")
        .put("path", PATCH_PATH)
        .put("value", "not-a-number");

    StepVerifier
        .create(validator.validate(CLIENT_NUMBER).apply(node))
        .expectError(ValidationException.class)
        .verify();
  }

  private static Stream<Arguments> validationAllowed() {
    return Stream.of(
        Arguments.of(NODE, true),
        Arguments.of(MAPPER.createObjectNode()
            .put("op", "replace")
            .put("path", PATCH_PATH)
            .put("value", 55.00), true),
        Arguments.of(MAPPER.createObjectNode()
            .put("op", "add")
            .put("path", PATCH_PATH)
            .put("value", 55.00), false)
    );
  }
  
}

