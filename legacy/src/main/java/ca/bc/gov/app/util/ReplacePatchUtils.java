package ca.bc.gov.app.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import tools.jackson.databind.JsonNode;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class ReplacePatchUtils {

  /**
   * Builds an update map from a JSON Patch, a field map, and extra fields.
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

    // Function that generates the update map value, based on the type of the value
    Function<JsonNode, Object> valueExtractor = node -> {
      if (!node.has("value") || node.get("value") == null || node.get("value").isNull()) {
        return null;
      }
      JsonNode valNode = node.get("value");
      if (valNode.isTextual()) {
        String text = valNode.asText();
        return StringUtils.isBlank(text) ? null : text;
      } else if (valNode.isBoolean()) {
        return valNode.asBoolean();
      } else if (valNode.isNumber()) {
        return valNode.numberValue();
      } else if (valNode.isArray()) {
        return StreamSupport
            .stream(valNode.spliterator(), false)
            .map(JsonNode::asText)
            .collect(Collectors.toList());
      } else {
        String value = valNode.asText();
        return value != null && !value.equals("null") ? value : null;
      }
    };

    // Function that generates the update map key
    Function<JsonNode, SqlIdentifier> keyExtractor = node -> SqlIdentifier.unquoted(
        fieldMap.get(node.get("path").asText()));

    // Filter the patch operations that are in the field map,
    // to prevent fields that are not supposed to be here.
    // Use LinkedHashMap to safely support null values (Collectors.toMap throws NPE on null).
    Map<SqlIdentifier, Object> updateMap = new LinkedHashMap<>();
    for (JsonNode entry : patch) {
      if (entry.has("path") && fieldMap.containsKey(entry.get("path").asText())) {
        updateMap.put(keyExtractor.apply(entry), valueExtractor.apply(entry));
      }
    }

    // If we have extra fields, such as updated user, we add them here
    if (extraFields != null) {
      for (Map.Entry<String, Object> entry : extraFields.entrySet()) {
        updateMap.put(SqlIdentifier.unquoted(entry.getKey()), entry.getValue());
      }
    }

    return updateMap;
  }

}
