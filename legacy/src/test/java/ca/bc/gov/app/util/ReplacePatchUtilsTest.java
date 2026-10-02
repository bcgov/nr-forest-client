package ca.bc.gov.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@DisplayName("Unit Test | Replace Patch Utils")
class ReplacePatchUtilsTest {

  private static final ObjectMapper mapper = new JsonMapper();

  private final Map<String, String> fieldMap = Map.of(
      "/contactName", "contact_name",
      "/emailAddress", "email_address",
      "/faxNumber", "fax_number",
      "/active", "active_ind",
      "/percentage", "percent_ownership",
      "/roles", "roles"
  );

  @Test
  @DisplayName("Build update with regular fields")
  void shouldBuildUpdateWithRegularFields() throws Exception {
    String json = """
        [
          {"op":"replace","path":"/contactName","value":"John Doe"}
        ]
        """;
    JsonNode patch = mapper.readTree(json);

    Map<SqlIdentifier, Object> result = ReplacePatchUtils.buildUpdate(
        patch,
        fieldMap,
        Map.of("extra_col", "extra_val")
    );

    assertEquals("John Doe", result.get(SqlIdentifier.unquoted("contact_name")));
    assertEquals("extra_val", result.get(SqlIdentifier.unquoted("extra_col")));
  }

  @Test
  @DisplayName("Build update with null and remove operations resulting in SQL null")
  void shouldBuildUpdateWithNullAndRemoveOperations() throws Exception {
    String json = """
        [
          {"op":"replace","path":"/emailAddress","value":null},
          {"op":"remove","path":"/faxNumber"}
        ]
        """;
    JsonNode patch = mapper.readTree(json);

    Map<SqlIdentifier, Object> result = ReplacePatchUtils.buildUpdate(
        patch,
        fieldMap,
        Map.of()
    );

    assertTrue(result.containsKey(SqlIdentifier.unquoted("email_address")));
    assertNull(result.get(SqlIdentifier.unquoted("email_address")));
    assertTrue(result.containsKey(SqlIdentifier.unquoted("fax_number")));
    assertNull(result.get(SqlIdentifier.unquoted("fax_number")));
  }

  @Test
  @DisplayName("Normalize empty and blank textual values to null")
  void shouldNormalizeBlankStringToNull() throws Exception {
    String json = """
        [
          {"op":"replace","path":"/emailAddress","value":""},
          {"op":"replace","path":"/faxNumber","value":"   "}
        ]
        """;
    JsonNode patch = mapper.readTree(json);

    Map<SqlIdentifier, Object> result = ReplacePatchUtils.buildUpdate(
        patch,
        fieldMap,
        Map.of()
    );

    assertTrue(result.containsKey(SqlIdentifier.unquoted("email_address")));
    assertNull(result.get(SqlIdentifier.unquoted("email_address")));
    assertTrue(result.containsKey(SqlIdentifier.unquoted("fax_number")));
    assertNull(result.get(SqlIdentifier.unquoted("fax_number")));
  }

  @Test
  @DisplayName("Handle boolean, number, and array data types")
  void shouldHandleDifferentDataTypes() throws Exception {
    String json = """
        [
          {"op":"replace","path":"/active","value":true},
          {"op":"replace","path":"/percentage","value":15.5},
          {"op":"replace","path":"/roles","value":["ADMIN","USER"]}
        ]
        """;
    JsonNode patch = mapper.readTree(json);

    Map<SqlIdentifier, Object> result = ReplacePatchUtils.buildUpdate(
        patch,
        fieldMap,
        Map.of()
    );

    assertEquals(true, result.get(SqlIdentifier.unquoted("active_ind")));
    assertEquals(15.5, result.get(SqlIdentifier.unquoted("percent_ownership")));
    assertEquals(List.of("ADMIN", "USER"), result.get(SqlIdentifier.unquoted("roles")));
  }

  @Test
  @DisplayName("Ignore operations with paths not in field map")
  void shouldIgnorePathsNotInFieldMap() throws Exception {
    String json = """
        [
          {"op":"replace","path":"/unknownPath","value":"test"},
          {"op":"replace","path":"/contactName","value":"Alice"}
        ]
        """;
    JsonNode patch = mapper.readTree(json);

    Map<SqlIdentifier, Object> result = ReplacePatchUtils.buildUpdate(
        patch,
        fieldMap,
        Map.of()
    );

    assertFalse(result.containsKey(SqlIdentifier.unquoted("unknownPath")));
    assertEquals("Alice", result.get(SqlIdentifier.unquoted("contact_name")));
  }

  @Test
  @DisplayName("Preserve operation order and extra fields without NullPointerException")
  void shouldPreserveOrderAndExtraFieldsWithoutNpe() throws Exception {
    String json = """
        [
          {"op":"remove","path":"/emailAddress"},
          {"op":"replace","path":"/contactName","value":"Bob"}
        ]
        """;
    JsonNode patch = mapper.readTree(json);

    Map<SqlIdentifier, Object> result = ReplacePatchUtils.buildUpdate(
        patch,
        fieldMap,
        Map.of("revision_count", 2L, "update_userid", "testUser")
    );

    assertEquals(4, result.size());
    assertNull(result.get(SqlIdentifier.unquoted("email_address")));
    assertEquals("Bob", result.get(SqlIdentifier.unquoted("contact_name")));
    assertEquals(2L, result.get(SqlIdentifier.unquoted("revision_count")));
    assertEquals("testUser", result.get(SqlIdentifier.unquoted("update_userid")));
  }
}
