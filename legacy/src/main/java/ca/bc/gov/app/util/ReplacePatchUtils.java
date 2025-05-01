package ca.bc.gov.app.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.relational.core.sql.SqlIdentifier;

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
      if (node.get("value").isTextual()) {
        return node.get("value").asText();
      } else if (node.get("value").isBoolean()) {
        return node.get("value").asBoolean();
      } else if (node.get("value").isNumber()) {
        return node.get("value").numberValue();
      } else if (node.get("value").isArray()) {
        return StreamSupport
            .stream(node.get("value").spliterator(), false)
            .map(JsonNode::asText)
            .collect(Collectors.toList());
      } else {
        String value = node.get("value").asText();
        return value != null && !value.equals("null") ? value : StringUtils.EMPTY;
      }
    };

    // Function that generates the update map key
    Function<JsonNode, SqlIdentifier> keyExtractor = node -> SqlIdentifier.unquoted(
        fieldMap.get(node.get("path").asText()));

    // Filter the patch operations that are in the field map,
    // to prevent fields that are not supposed to be here
    Map<SqlIdentifier, Object> updateMap = StreamSupport
        .stream(patch.spliterator(), false)
        .filter(entry -> fieldMap.containsKey(entry.get("path").asText()))
        .collect(
            Collectors.toMap(
                keyExtractor,
                valueExtractor
            )
        );

    // If we have extra fields, such as updated user, we add them here
    if (extraFields != null) {
      updateMap.putAll(
          extraFields
              .entrySet().stream()
              .collect(
                  Collectors.toMap(
                      entry -> SqlIdentifier.unquoted(entry.getKey()),
                      Map.Entry::getValue
                  )
              )
      );
    }

    return updateMap;
  }

}
