package ca.bc.gov.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.bc.gov.app.exception.CannotApplyPatchException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Unit Test | Patch Utils")
class PatchUtilsTest {

  private static final ObjectMapper mapper = new ObjectMapper();

  public static final String SIMPLE = """
      [
        {"op":"replace","path":"/value","value":"1234"}
      ]"""
      .replace(StringUtils.LF, StringUtils.EMPTY)
      .replace(StringUtils.SPACE, StringUtils.EMPTY);

  public static final String CONTENT = """
      [
        {"op":"replace","path":"/value","value":"1234"},
        {"op":"replace","path":"/entries/0/personalId","value":"1234"},
        {"op":"replace","path":"/user/name","value":"1234"},
        {"op":"remove","path":"/entries/1"},
        {"op":"add","path":"/entries/2","value":{"value:":"44722"}}
      ]"""
      .replace(StringUtils.LF, StringUtils.EMPTY)
      .replace(StringUtils.SPACE, StringUtils.EMPTY);

  public static final String ADD = """
      [
        {"op":"add","path":"/entries/2","value":{"value:":"44722"}}
      ]"""
      .replace(StringUtils.LF, StringUtils.EMPTY)
      .replace(StringUtils.SPACE, StringUtils.EMPTY);

  @Test
  @DisplayName("Apply patch")
  void shouldApplyPatch() throws JsonProcessingException {
    JsonNode patchNode = toNode(SIMPLE);
    TestEntity result = PatchUtils.patchClient(patchNode, new TestEntity("abc123"),
        TestEntity.class, mapper);
    assertEquals("1234", result.getValue());
  }

  @Test
  @DisplayName("Fail when patching with invalid value")
  void shouldFailWhenPatching() throws Exception {
    JsonNode patchNode = toNode(CONTENT.replace("replace", "join"));
    TestEntity abc123 = new TestEntity("abc123");
    assertThrows(CannotApplyPatchException.class, () ->
        PatchUtils.patchClient(patchNode, abc123, TestEntity.class, mapper));
  }

  @ParameterizedTest
  @DisplayName("Check and allow or deny patch op")
  @CsvSource({"'value',true", "'value/1',false", "'name',false"})
  void shouldCheckAndAllowOrDenyPatchOp(String path, boolean exist) throws JsonProcessingException {
    JsonNode patch = toNode(CONTENT);
    assertEquals(exist, PatchUtils.checkOperation(patch, path, mapper));
  }

  @ParameterizedTest
  @MethodSource("filterOps")
  @DisplayName("Filter patch ops")
  void shouldFilterPatchOps(String prefix, List<String> paths, String expectation)
      throws JsonProcessingException {
    JsonNode patch = toNode(CONTENT);
    JsonNode result = PatchUtils.filterPatchOperations(patch, prefix, paths, mapper);
    assertEquals(expectation, result.toString());
  }

  @ParameterizedTest
  @CsvSource({
      "'/value','user','/value'",
      "'/user/name','user','/name'",
      "'/entries/0/personalId','entries','/0/personalId'"
  })
  @DisplayName("Remove prefixes")
  void shouldRemovePrefixes(String path, String prefix, String expectation) {
    assertEquals(expectation, PatchUtils.removePrefix(path, prefix));
  }

  @Test
  @DisplayName("Load IDs")
  void shouldLoadIds() throws JsonProcessingException {
    JsonNode patch = toNode(CONTENT);
    assertIterableEquals(Set.of("0"), PatchUtils.loadIds(patch));
  }

  @Test
  @DisplayName("Load ID")
  void shouldLoadId() throws JsonProcessingException {
    JsonNode patch = toNode(
        "{\"op\":\"replace\",\"path\":\"/entries/0/personalId\",\"value\":\"1234\"}"
    );
    assertEquals("0", PatchUtils.loadId(patch));
  }

  @Test
  @DisplayName("Merge two nodes")
  void shouldMergeNodes() throws JsonProcessingException {
    JsonNode expectation = toNode("[{\"value\":\"1234\"},{\"value\":\"5678\"}]");
    JsonNode node1 = createValueNode("1234");
    JsonNode node2 = createValueNode("5678");
    JsonNode result = PatchUtils.mergeNodes().apply(node1, node2);
    assertEquals(expectation, result);
  }

  @Test
  @DisplayName("Merge node to array")
  void shouldMergeNodesIfArray() throws JsonProcessingException {
    JsonNode expectation = toNode("[{\"value\":\"5678\"}]");
    JsonNode node1 = mapper.createArrayNode();
    JsonNode node2 = createValueNode("5678");
    JsonNode result = PatchUtils.mergeNodes().apply(node1, node2);
    assertEquals(expectation, result);
  }

  @Test
  @DisplayName("Filter by ID")
  void shouldFilterById() throws JsonProcessingException {
    JsonNode expectation = toNode(
        "[{\"op\":\"replace\",\"path\":\"/entries/0/personalId\",\"value\":\"1234\"}]"
    );

    assertEquals(expectation,PatchUtils.filterById(toNode(CONTENT), mapper).apply("0"));
  }

  @MethodSource("idsAndSubIds")
  @ParameterizedTest
  @DisplayName("Load IDs and sub-IDs")
  void shouldLoadIdsAndSubIds(
      JsonNode node, String expectedId, String expectedSubId) throws JsonProcessingException {
    PatchUtils.loadIdsAndSubIds(node).forEach((id, subId) -> {
      assertEquals(expectedId, id);
      if (expectedSubId == null) {
        assertNull(subId);
      } else {
        assertTrue(subId.contains(expectedSubId));
      }
    });
  }

  private static Stream<Arguments> filterOps() {
    return Stream.of(
        Arguments.argumentSet(
            "With prefix and restricted paths",
            "user",
            List.of("/value", "/user"),
            SIMPLE
        ),
        Arguments.argumentSet(
            "With no prefix and no restricted paths",
            null,
            List.of(),
            CONTENT
        ),
        Arguments.argumentSet(
            "With no prefix and restricted paths",
            null,
            List.of("/value", "/user"),
            SIMPLE
        ),

        Arguments.argumentSet(
            "With no prefix and restricted paths",
            "user",
            List.of(),
            CONTENT.replace("/user", StringUtils.EMPTY)
        )
    );
  }

  private static Stream<Arguments> idsAndSubIds(){
    return Stream.of(
        Arguments.argumentSet(
            "With ID and sub-ID",
            createPathNode("/relatedClients/00/1/relatedClient/location/code"),"00","1"
        ),
        Arguments.argumentSet(
            "With ID and sub-ID and extended path",
            createPathNode("/relatedClients/00/0/relatedClient/location/code"),"00","0"
        ),
        Arguments.argumentSet(
            "With ID and no sub-ID",
            createPathNode("/relatedClients/00"),"00",null
        ),
        Arguments.argumentSet(
            "With ID and no sub-ID and extended path",
            createPathNode("/relatedClients/00/relatedClient/location/code"),"00",null
        ),
        Arguments.argumentSet(
            "Started with ID and sub-ID",
            createPathNode("/00/1/relatedClient/location/code"),"00","1"
        ),
        Arguments.argumentSet(
            "Started with ID and sub-ID and extended path",
            createPathNode("/00/0/relatedClient/location/code"),"00","0"
        ),
        Arguments.argumentSet(
            "Started with ID and no sub-ID",
            createPathNode("/00"),"00",null
        ),
        Arguments.argumentSet(
            "Started with ID and no sub-ID and extended path",
            createPathNode("/00/relatedClient/location/code"),"00",null
        )
    );
  }

  private static JsonNode toNode(String content) throws JsonProcessingException {
    return mapper.readValue(content,JsonNode.class);
  }

  private static JsonNode createValueNode(String value) {
    return mapper.createObjectNode().put("value", value);
  }

  private static JsonNode createPathNode(String value) {
    return mapper.createObjectNode().put("path", value);
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  private static class TestEntity {

    private String value;
  }

}