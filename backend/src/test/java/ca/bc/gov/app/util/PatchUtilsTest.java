package ca.bc.gov.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.function.BinaryOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit Test | PatchUtils")
class PatchUtilsTest {

  private ObjectMapper mapper;
  private BinaryOperator<JsonNode> mergeOperator;

  @BeforeEach
  void setUp() {
    mapper = new ObjectMapper();
    mergeOperator = PatchUtils.mergeNodes(mapper);
  }

  @Test
  @DisplayName("mergeNodes should return ArrayNode when merging two ObjectNodes")
  void shouldReturnArrayNodeWhenMergingTwoObjectNodes() {
    ObjectNode node1 = mapper.createObjectNode();
    node1.put("key1", "value1");

    ObjectNode node2 = mapper.createObjectNode();
    node2.put("key2", "value2");

    JsonNode result = mergeOperator.apply(node1, node2);

    assertInstanceOf(ArrayNode.class, result);
    ArrayNode arrayResult = (ArrayNode) result;
    assertEquals(2, arrayResult.size());
    assertEquals("value1", arrayResult.get(0).get("key1").asText());
    assertEquals("value2", arrayResult.get(1).get("key2").asText());
  }

  @Test
  @DisplayName("mergeNodes should add node2 to existing ArrayNode when node1 is ArrayNode")
  void shouldAddNode2ToExistingArrayNodeWhenNode1IsArrayNode() {
    ArrayNode node1 = mapper.createArrayNode();
    node1.add(mapper.createObjectNode().put("existing", "item"));

    ObjectNode node2 = mapper.createObjectNode();
    node2.put("new", "item");

    JsonNode result = mergeOperator.apply(node1, node2);

    assertInstanceOf(ArrayNode.class, result);
    ArrayNode arrayResult = (ArrayNode) result;
    assertEquals(2, arrayResult.size());
    assertEquals("item", arrayResult.get(0).get("existing").asText());
    assertEquals("item", arrayResult.get(1).get("new").asText());
  }

  @Test
  @DisplayName("mergeNodes should merge two ArrayNodes into one")
  void shouldMergeTwoArrayNodesIntoOne() {
    ArrayNode node1 = mapper.createArrayNode();
    node1.add(mapper.createObjectNode().put("key1", "value1"));
    node1.add(mapper.createObjectNode().put("key2", "value2"));

    ArrayNode node2 = mapper.createArrayNode();
    node2.add(mapper.createObjectNode().put("key3", "value3"));
    node2.add(mapper.createObjectNode().put("key4", "value4"));

    JsonNode result = mergeOperator.apply(node1, node2);

    assertInstanceOf(ArrayNode.class, result);
    ArrayNode arrayResult = (ArrayNode) result;
    assertEquals(4, arrayResult.size());
    assertEquals("value1", arrayResult.get(0).get("key1").asText());
    assertEquals("value2", arrayResult.get(1).get("key2").asText());
    assertEquals("value3", arrayResult.get(2).get("key3").asText());
    assertEquals("value4", arrayResult.get(3).get("key4").asText());
  }

  @Test
  @DisplayName("mergeNodes should handle ArrayNode as node1 with non-ArrayNode as node2")
  void shouldHandleArrayNodeAsNode1WithNonArrayNodeAsNode2() {
    ArrayNode node1 = mapper.createArrayNode();
    node1.add("item1");
    node1.add("item2");

    JsonNode node2 = mapper.valueToTree("item3");

    JsonNode result = mergeOperator.apply(node1, node2);

    assertInstanceOf(ArrayNode.class, result);
    ArrayNode arrayResult = (ArrayNode) result;
    assertEquals(3, arrayResult.size());
    assertEquals("item1", arrayResult.get(0).asText());
    assertEquals("item2", arrayResult.get(1).asText());
    assertEquals("item3", arrayResult.get(2).asText());
  }

  @Test
  @DisplayName("mergeNodes should not modify original ArrayNode when node1 is ArrayNode")
  void shouldNotModifyOriginalArrayNodeWhenNode1IsArrayNode() {
    ArrayNode node1 = mapper.createArrayNode();
    node1.add("original");

    ObjectNode node2 = mapper.createObjectNode().put("new", "value");

    int originalSize = node1.size();
    mergeOperator.apply(node1, node2);

    assertEquals(originalSize, node1.size());
  }

  @Test
  @DisplayName("mergeNodes should handle primitive values")
  void shouldHandlePrimitiveValues() {
    JsonNode node1 = mapper.valueToTree("string1");
    JsonNode node2 = mapper.valueToTree("string2");

    JsonNode result = mergeOperator.apply(node1, node2);

    assertInstanceOf(ArrayNode.class, result);
    ArrayNode arrayResult = (ArrayNode) result;
    assertEquals(2, arrayResult.size());
    assertEquals("string1", arrayResult.get(0).asText());
    assertEquals("string2", arrayResult.get(1).asText());
  }

  @Test
  @DisplayName("mergeNodes should handle numeric values")
  void shouldHandleNumericValues() {
    JsonNode node1 = mapper.valueToTree(123);
    JsonNode node2 = mapper.valueToTree(456);

    JsonNode result = mergeOperator.apply(node1, node2);

    assertInstanceOf(ArrayNode.class, result);
    ArrayNode arrayResult = (ArrayNode) result;
    assertEquals(2, arrayResult.size());
    assertEquals(123, arrayResult.get(0).asInt());
    assertEquals(456, arrayResult.get(1).asInt());
  }

  @Test
  @DisplayName("mergeNodes should return empty ArrayNode with empty ArrayNodes")
  void shouldReturnEmptyArrayNodeWithEmptyArrayNodes() {
    ArrayNode node1 = mapper.createArrayNode();
    ArrayNode node2 = mapper.createArrayNode();

    JsonNode result = mergeOperator.apply(node1, node2);

    assertInstanceOf(ArrayNode.class, result);
    ArrayNode arrayResult = (ArrayNode) result;
    assertTrue(arrayResult.isEmpty());
  }

  @Test
  @DisplayName("mergeNodes should handle boolean values")
  void shouldHandleBooleanValues() {
    JsonNode node1 = mapper.valueToTree(true);
    JsonNode node2 = mapper.valueToTree(false);

    JsonNode result = mergeOperator.apply(node1, node2);

    assertInstanceOf(ArrayNode.class, result);
    ArrayNode arrayResult = (ArrayNode) result;
    assertEquals(2, arrayResult.size());
    assertTrue(arrayResult.get(0).asBoolean());
    assertEquals(false, arrayResult.get(1).asBoolean());
  }
}

