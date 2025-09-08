package ca.bc.gov.app.controller;

import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@DisplayName("Integrated Test | Client Search for Related Clients Controller")
class ClientSearchRelatedClientIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @ParameterizedTest
  @MethodSource("byRelatedSearch")
  @DisplayName("Search by related client")
  void shouldSearchByRelatedClient(
      String clientNumber,
      String value,
      String type,
      List<String> expectedClientNames
  ) {

    var resultCheck =
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/search/relation/{clientNumber}")
                .queryParam("value", value)
                .queryParamIfPresent("type", Optional.ofNullable(type))
                .build(clientNumber)
        )
        .header("Content-Type", "application/json")
        .exchange()
        .expectStatus().isOk()
        .expectHeader().exists("X-Total-Count")
        .expectHeader().valueEquals("X-Total-Count", String.valueOf(expectedClientNames.size()))
        .expectBody();

    if (expectedClientNames.isEmpty()) {
      resultCheck.json("[]");
    } else {
      forEachIndexed(expectedClientNames, (index, name) ->
          resultCheck.jsonPath("$[" + index + "].clientFullName").isEqualTo(name)
      );
      resultCheck.consumeWith(System.out::println);
    }

  }

  private static Stream<Arguments> byRelatedSearch() {
    return
        Stream
            .of(
                Arguments.argumentSet(
                    "Search by group, with no type for OAK HERITAGE GROUP",
                    "00000158",
                    "group",
                    null,
                    List.of("KLEIN GROUP")
                ),
                Arguments.argumentSet(
                    "James Hunt looking for other James",
                    "00000009",
                    "james",
                    null,
                    List.of("JAMES BAXTER (BAXTER'S FAMILY)", "james bond bond (Elaricho)", "james hunt hunt", "jjamess bbondd bbondd")
                ),
                Arguments.argumentSet(
                    "James Hunt looking for other FM James",
                    "00000009",
                    "james",
                    "FM",
                    List.of("JAMES BAXTER (BAXTER'S FAMILY)","james bond bond (Elaricho)","james hunt hunt")
                ),
                Arguments.argumentSet(
                    "James Hunt looking for other SH James",
                    "00000009",
                    "james",
                    "SH",
                    List.of("jjamess bbondd bbondd")
                )

            );
  }

  /**
   * Iterates over a list and applies a given consumer function to each element along with its index.
   *
   * @param <T> The type of elements in the list.
   * @param list The list of elements to iterate over.
   * @param consumer A BiConsumer that takes the index and the element at that index.
   *                 The consumer is applied to each element in the list.
   */
  private static <T> void forEachIndexed(List<T> list, BiConsumer<Integer, T> consumer) {
    for (int i = 0; i < list.size(); i++) {
      consumer.accept(i, list.get(i));
    }
  }

}
