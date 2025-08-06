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

@DisplayName("Integrated Test | Patch Validator : Related Clients - Location")
public class RelatedClientsLocationPatchValidatorIntegrationTest
    extends AbstractTestContainerIntegrationTest {

  @Autowired
  private RelatedClientsLocationPatchValidator validator;

  public static final String PATCH_PATH = "/relatedClients/00/0/client/location";
  public static final String CLIENT_NUMBER = "01000001";
  public static final ObjectMapper MAPPER = new ObjectMapper();

  private static final JsonNode NODE = MAPPER.createObjectNode()
      .put("op", "replace")
      .put("path", PATCH_PATH)
      .set("value", MAPPER.createObjectNode()
          .put("code", "00")
          .put("name", "Mailing address"));

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

  private static Stream<Arguments> validationAllowed() {
    return Stream.of(
        Arguments.of(NODE, true),
        Arguments.of(MAPPER.createObjectNode()
            .put("op", "add")
            .put("path", PATCH_PATH)
            .set("value", MAPPER.createObjectNode()
                .put("code", "00")
                .put("name", "Mailing address")), false)
    );
  }
  
  @Test
  @DisplayName("Should fail if location code is blank")
  void shouldFailIfCodeIsBlank() {
    JsonNode invalidNode = MAPPER.createObjectNode()
        .put("op", "replace")
        .put("path", PATCH_PATH)
        .set("value", MAPPER.createObjectNode()
            .put("code", "")
            .put("name", "Mailing address"));

    StepVerifier
        .create(validator.validate(CLIENT_NUMBER).apply(invalidNode))
        .expectError(ValidationException.class)
        .verify();
  }
  
}
