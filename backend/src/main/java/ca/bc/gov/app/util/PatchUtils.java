package ca.bc.gov.app.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.function.BinaryOperator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class PatchUtils {

  public static BinaryOperator<JsonNode> mergeNodes(ObjectMapper mapper) {
    return (node1, node2) -> {
      ArrayNode arrayNode = mapper.createArrayNode();
      if (node1 instanceof ArrayNode) {
        arrayNode = node1.deepCopy();
      } else {
        arrayNode.add(node1);
      }
      if (node2 instanceof ArrayNode anode2) {
        arrayNode.addAll(anode2);
      } else {
        arrayNode.add(node2);
      }
      return arrayNode;
    };
  }

}
