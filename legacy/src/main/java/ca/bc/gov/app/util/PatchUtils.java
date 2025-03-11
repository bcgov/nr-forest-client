package ca.bc.gov.app.util;


import ca.bc.gov.app.exception.CannotApplyPatchException;
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
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class PatchUtils {


  /**
   * Applies a JSON Patch to a target object and returns the patched object.
   *
   * @param <T>         the type of the target object
   * @param patch       the JSON Patch to apply
   * @param target      the target object to patch
   * @param entityClass the class of the target object
   * @param mapper      the ObjectMapper to use for JSON processing
   * @return the patched object
   * @throws CannotApplyPatchException if an error occurs while applying the patch
   */
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
      throw new CannotApplyPatchException("Error while applying patch to Object", e);
    }
  }

  /**
   * Checks if any operation in the given JSON Patch has a path that starts with the specified path
   * prefix.
   *
   * @param patch     the JSON Patch to check
   * @param checkPath the path prefix to check for
   * @param mapper    the ObjectMapper to use for JSON processing
   * @return true if any operation's path starts with the specified path prefix, false otherwise
   */
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

  /**
   * Filters the operations in a JSON Patch based on a specified prefix and restricted paths.
   *
   * @param patch           the JSON Patch to filter
   * @param prefix          the prefix to filter the operations by
   * @param restrictedPaths the list of restricted paths to filter the operations by
   * @param mapper          the ObjectMapper to use for JSON processing
   * @return a JsonNode containing the filtered operations
   */
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

  /**
   * Filters the operations in a JSON Patch based on a specified prefix and restricted paths.
   *
   * @param patch           the JSON Patch to filter
   * @param prefix          the prefix to filter the operations by
   * @param restrictedPaths the list of restricted paths to filter the operations by
   * @param mapper          the ObjectMapper to use for JSON processing
   * @return a JsonNode containing the filtered operations
   */
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

  /**
   * Removes the specified prefix from the given path.
   *
   * @param path   the original path
   * @param prefix the prefix to remove
   * @return the path without the prefix, or the original path if the prefix is not present
   */
  public static String removePrefix(String path, String prefix) {

    // We generate the prefixed path for later use
    String prefixedPath = String.format("/%s", prefix);

    // If the path starts with the prefixed path
    if (path.startsWith(prefixedPath)) {
      // We generate a new operation path without the prefix
      return path.substring(prefixedPath.length());
    }
    return path;
  }

  /**
   * Extracts and returns a set of unique IDs from the given filtered JSON Patch operations.
   *
   * @param filteredNode the JsonNode containing the filtered JSON Patch operations
   * @return a Set of unique IDs extracted from the paths of the JSON Patch operations
   */
  public static Set<String> loadIds(JsonNode filteredNode) {
    Set<String> ids = new HashSet<>();
    filteredNode.forEach(node -> ids.add(loadId(node)));
    return ids.stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet());
  }

  public static String loadId(JsonNode node) {
    String id = PatchUtils.extractPathInfo(node.get("path").asText()).getLeft();
    if (StringUtils.isNotBlank(id)) {
      return id;
    }
    return StringUtils.EMPTY;
  }
}
