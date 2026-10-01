package ca.bc.gov.app.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import tools.jackson.databind.JsonNode;

/**
 * Utility class for building database update maps from JSON Patch operations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class ReplacePatchUtils {

  /**
   * Builds an update map from a JSON Patch, a field map, and extra fields.
   *
   * @param patch The JSON Patch to build the update map from
   * @param fieldMap The field map to use for mapping the patch paths to the database columns
   * @param extraFields Extra fields to include in the update map
   * @return a Map containing the update values
   */
  public static Map<SqlIdentifier, Object> buildUpdate(
      JsonNode patch,
      Map<String, String> fieldMap,
      Map<String, Object> extraFields
  ) {
    Map<SqlIdentifier, Object> updateMap = new LinkedHashMap<>();

    if (patch != null && fieldMap != null) {
      for (JsonNode entry : patch) {
        if (entry.has("path")) {
          String path = entry.get("path").asText();
          if (fieldMap.containsKey(path)) {
            updateMap.put(
                SqlIdentifier.unquoted(fieldMap.get(path)),
                extractValue(entry)
            );
          }
        }
      }
    }

    if (extraFields != null) {
      extraFields.forEach((key, value) ->
          updateMap.put(SqlIdentifier.unquoted(key), value)
      );
    }

    return updateMap;
  }

  /**
   * Extracts the typed value from a JSON Patch entry node.
   *
   * @param node the patch operation node
   * @return the extracted value, or {@code null} if absent or blank
   */
  private static Object extractValue(JsonNode node) {
    if (!node.has("value") || node.get("value") == null || node.get("value").isNull()) {
      return null;
    }
    JsonNode valNode = node.get("value");
    if (valNode.isTextual()) {
      String text = valNode.asText();
      return StringUtils.isBlank(text) ? null : text;
    }
    if (valNode.isBoolean()) {
      return valNode.asBoolean();
    }
    if (valNode.isNumber()) {
      return valNode.numberValue();
    }
    if (valNode.isArray()) {
      return StreamSupport
          .stream(valNode.spliterator(), false)
          .map(JsonNode::asText)
          .collect(Collectors.toList());
    }
    String value = valNode.asText();
    return value != null && !value.equals("null") ? value : null;
  }

}
