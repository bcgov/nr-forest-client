package ca.bc.gov.app.service.partial;

import ca.bc.gov.app.exception.CannotApplyPatchException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.JsonPatch;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

public interface ClientPartialService<T> {

  String getPrefix();
  List<String> getRestrictedPaths();
  Mono<Void> applyPatch(String clientNumber, JsonPatch patch, ObjectMapper mapper);

  default T patchClient(
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

  /**
   * Checks if the given JsonPatch contains any operations that start with the specified path. This
   * is to determine if a specific part of the object is being modified, to allow for a more
   * granular update.
   *
   * @param patch     the JsonPatch to check
   * @param checkPath the path to look for in the patch operations
   * @param mapper    the ObjectMapper to use for conversion
   * @return true if any operation starts with the specified path, false otherwise
   */
  default boolean checkOperation(
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


  default JsonNode filterPatchOperations(
      JsonPatch patch,
      ObjectMapper mapper
  ) {

    // A new ArrayNode to store the filtered operations
    ArrayNode filteredNode = mapper.createArrayNode();

    // First, jackson's ObjectMapper is used to convert the JsonPatch to a JsonNode
    mapper
        .convertValue(patch, JsonNode.class)
        // Then for each operation in the JsonNode we do
        .forEach(operation -> {
          // Get the path of the operation
          String path = operation.get("path").asText();
          // We generate the prefixed path for later use
          String prefixedPath = String.format("/%s", getPrefix());

          // If the path starts with the prefixed path
          if (path.startsWith(prefixedPath)) {
            // We generate a new operation path without the prefix
            String newPath = path.replace(prefixedPath, StringUtils.EMPTY);
            // This variable here initially is just a copy of the above but it is used to
            // check individual fields in the restricted paths for array/list data
            String patchCheck = newPath;

            //List entries starts with a number,
            // so we take that into consideration as well when checking the restricted paths
            // by using a regex to check if it matches the format /\d+/.*
            Pattern pattern = Pattern.compile("/(\\d+)/(.*)");
            Matcher matcher = pattern.matcher(newPath);

            // When it matches, it means that this entry is a list entry
            if(matcher.find()){
              // So we extract just the field name from the path
              patchCheck = String.format("/%s",matcher.group(2));
            }

            // If we don't have a list of restricted fields or
            // if we the current field is part of the restricted fields
            if(
                getRestrictedPaths().isEmpty() ||
                getRestrictedPaths().stream().anyMatch(patchCheck::equals)
            ) {
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

}
