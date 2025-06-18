package ca.bc.gov.app.validator.patch;

import ca.bc.gov.app.ApplicationConstant;
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

@DisplayName("Integrated Test | Patch Validator : Admin Role")
class AdminRolePatchValidatorIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  private AdminRolePatchValidator validator;

  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static final JsonNode ADMIN_DATA = MAPPER
      .createObjectNode()
      .put("op", "replace")
      .put("path", "/client/clientName")
      .put("value", "ABC");

  public static final JsonNode OTHER_DATA = MAPPER
      .createObjectNode()
      .put("op", "replace")
      .put("path", "/client/acronym")
      .put("value", "ABC");

  public static final JsonNode ADMIN = MAPPER.createObjectNode()
      .put("path","/roles")
      .put("roles", ApplicationConstant.ROLE_ADMIN)
      .put("userId", "test");

  public static final JsonNode EDITOR = MAPPER.createObjectNode()
      .put("path","/roles")
      .put("roles", ApplicationConstant.ROLE_EDITOR)
      .put("userId", "test");

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
        .create(
            validator
                .globalValidator(
                    MAPPER
                        .createArrayNode()
                        .add(OTHER_DATA)
                        .add(ADMIN),
                    "00000001"
                ).apply(ADMIN))
        .expectNext(ADMIN)
        .verifyComplete();
  }

  @Test
  @DisplayName("Should fail")
  void shouldFailDueToRole() {

    StepVerifier
        .create(
            validator
                .globalValidator(
                    MAPPER
                        .createArrayNode()
                        .add(ADMIN_DATA)
                        .add(EDITOR),
                    "00000001"
                ).apply(EDITOR))
        .expectError(ValidationException.class)
        .verify();
  }

  private static Stream<Arguments> validationAllowed() {
    return Stream
        .of(
            Arguments.of(ADMIN, false),
            Arguments.of(EDITOR, true),
            Arguments.of(OTHER_DATA, false),
            Arguments.of(ADMIN_DATA, false)
        );
  }


}
