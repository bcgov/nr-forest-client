package ca.bc.gov.app.utils;

import ca.bc.gov.app.exception.CannotApplyPatchException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.JsonPatch;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class PatchUtils {


  public static <T> T patchClient(
      JsonNode patch,
      T target,
      Class<T> entityClass,
      ObjectMapper mapper
  ) {
    try {
      // Convert the original entity object to a JsonNode for patching.
      JsonNode node = mapper.convertValue(target, JsonNode.class);

      // This maps the JsonNode to a JsonPatch object.
      JsonPatch filteredPatch = mapper.treeToValue(patch, JsonPatch.class);

      // Apply the patch to the original object in order to create a new object with the changes.
      JsonNode patched = filteredPatch.apply(node);

      // Convert the JsonNode back to the entity object to be saved
      return mapper.treeToValue(patched, entityClass);
    } catch (Exception e) {
      throw new CannotApplyPatchException("Error while applying patch to customer");
    }
  }

  public static boolean checkOperation(
      JsonPatch patch,
      String checkPath,
      ObjectMapper mapper
  ) {
    // This is just to make sure is thread safe
    AtomicBoolean filteredNode = new AtomicBoolean(false);
    // First, jackson's ObjectMapper is used to convert the JsonPatch to a JsonNode
    mapper
        .convertValue(patch, JsonNode.class)
        // Then for each operation in the JsonNode we do
        .forEach(operation -> {
          // A check if the path of the operation starts with the specified path prefix
          if (operation.get("path").asText().startsWith(String.format("/%s", checkPath))) {
            filteredNode.set(true);
          }
        });

    // Return the result of the check
    return filteredNode.get();
  }

  public static JsonNode filterPatchOperations(
      JsonPatch patch,
      String prefix,
      List<String> restrictedPaths,
      ObjectMapper mapper
  ) {
    return filterPatchOperations(
        mapper.convertValue(patch, JsonNode.class),
        prefix,
        restrictedPaths,
        mapper);
  }

  public static JsonNode filterPatchOperations(
      JsonNode patch,
      String prefix,
      List<String> restrictedPaths,
      ObjectMapper mapper
  ) {

    // A new ArrayNode to store the filtered operations
    ArrayNode filteredNode = mapper.createArrayNode();

    // First, jackson's ObjectMapper is used to convert the JsonPatch to a JsonNode
    patch
        // Then for each operation in the JsonNode we do
        .forEach(operation -> {
          // Get the path of the operation
          String path = operation.get("path").asText();
          // If the path starts with the prefixed path
          if (StringUtils.isNotBlank(prefix) && path.startsWith(String.format("/%s", prefix))) {
            // We generate a new operation path without the prefix
            String newPath = removePrefix(path, prefix);
            // This variable here initially is just a copy of the above, but it is used to
            // check individual fields in the restricted paths for array/list data
            String patchCheck = extractPathInfo(newPath).getRight();

            // If we don't have a list of restricted fields or
            // if we the current field is part of the restricted fields
            if (
                restrictedPaths.isEmpty() ||
                restrictedPaths.stream().anyMatch(patchCheck::equals)
            ) {
              // We create a deep copy of the operation
              ObjectNode updatedOperation = operation.deepCopy();
              // Then we update the path of the operation
              updatedOperation.put("path", newPath);
              // Finally we add the updated operation to the filteredNode
              filteredNode.add(updatedOperation);
            }

          } else {
            // This variable here initially is just a copy of the above, but it is used to
            // check individual fields in the restricted paths for array/list data
            String patchCheck = extractPathInfo(path).getRight();

            // If we don't have a list of restricted fields or
            // if we the current field is part of the restricted fields
            if (
                restrictedPaths.isEmpty() ||
                restrictedPaths.stream().anyMatch(patchCheck::equals)
            ) {
              // Finally we add the operation to the filteredNode
              filteredNode.add(operation);
            }
          }

        });

    return filteredNode;
  }

  public static JsonNode filterOperationsByOp(
      JsonPatch patch,
      String operationName,
      String prefix,
      ObjectMapper mapper
  ) {
    return filterOperationsByOp(
        patch,
        operationName,
        prefix,
        List.of(),
        mapper);
  }

  public static JsonNode filterOperationsByOp(
      JsonPatch patch,
      String operationName,
      String prefix,
      List<String> restrictedPaths,
      ObjectMapper mapper
  ) {
    return filterOperationsByOp(
        mapper.convertValue(patch, JsonNode.class),
        operationName,
        prefix,
        restrictedPaths,
        mapper);
  }

  public static JsonNode filterOperationsByOp(
      JsonNode patch,
      String operationName,
      String prefix,
      List<String> restrictedPaths,
      ObjectMapper mapper
  ) {

    // A new ArrayNode to store the filtered operations
    ArrayNode filteredNode = mapper.createArrayNode();

    // First, jackson's ObjectMapper is used to convert the JsonPatch to a JsonNode
    patch
        // Then for each operation in the JsonNode we do
        .forEach(operation -> {
          // Get the path of the operation
          String path = operation.get("path").asText();
          // If the path starts with the prefixed path
          if (
              StringUtils.isNotBlank(prefix)
              && path.startsWith(String.format("/%s", prefix))
              && operation.get("op").asText().equals(operationName)
          ) {
            // We generate a new operation path without the prefix
            String newPath = removePrefix(path, prefix);

            if (restrictedPaths.isEmpty() || restrictedPaths.stream().anyMatch(newPath::endsWith)) {
              // We create a deep copy of the operation
              ObjectNode updatedOperation = operation.deepCopy();
              // Then we update the path of the operation
              updatedOperation.put("path", newPath);
              // Finally we add the updated operation to the filteredNode
              filteredNode.add(updatedOperation);
            }
          }
        });

    return filteredNode;
  }

  public static <T> T loadAddValue(
      JsonNode patch,
      Class<T> entityClass,
      ObjectMapper mapper
  ) {
    try {
      return mapper.readValue(patch.get("value").toPrettyString(), entityClass);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Extracts information from a given path. This method checks if the path matches the format
   * /\d+/.* to determine if it is a list entry. If it matches, it extracts the field name and the
   * id. Otherwise, it returns the path as is.
   *
   * @param path the path to extract information from
   * @return a Pair containing the id (if present) and the remaining path
   */
  public static Pair<String, String> extractPathInfo(String path) {
    //List entries starts with a number,
    // so we take that into consideration as well when checking the restricted paths
    // by using a regex to check if it matches the format /\d+/.*
    Pattern pattern = Pattern.compile("/(\\d+)/(.*)");
    Matcher matcher = pattern.matcher(path);

    // When it matches, it means that this entry is a list entry
    if (matcher.find()) {
      // So we extract the field name and the id
      return Pair.of(matcher.group(1), String.format("/%s", matcher.group(2)));
    } else {
      // Otherwise we just return the path as is
      return Pair.of(null, path);
    }
  }

  public static String removePrefix(String path, String prefix) {

    // We generate the prefixed path for later use
    String prefixedPath = String.format("/%s", prefix);

    // If the path starts with the prefixed path
    if (path.startsWith(prefixedPath)) {
      // We generate a new operation path without the prefix
      return path.replace(prefixedPath, StringUtils.EMPTY);
    }
    return path;
  }

  public static Set<String> loadIds(JsonNode filteredNode) {
    Set<String> ids = new HashSet<>();

    filteredNode.forEach(node -> {
      String id = PatchUtils.extractPathInfo(node.get("path").asText()).getLeft();
      if (StringUtils.isNotBlank(id)) {
        ids.add(id);
      }
    });
    return ids;
  }

}
