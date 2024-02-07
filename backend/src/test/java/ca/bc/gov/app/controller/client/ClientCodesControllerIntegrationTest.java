package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;

@DisplayName("Integrated Test | FSA Client Codes Controller")
class ClientCodesControllerIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @BeforeEach
  public void reset() {
    client = client.mutate()
        .responseTimeout(Duration.ofSeconds(10))
        .build();
  }

  @Test
  @DisplayName("Codes are in expected order")
  void shouldListCodesAsExpected() {

    client
        .get()
        .uri("/api/codes/clientTypes")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].code").isNotEmpty()
        .jsonPath("$[0].code").isEqualTo("A")

        .jsonPath("$[1].code").isNotEmpty()
        .jsonPath("$[1].code").isEqualTo("C");

  }

  @ParameterizedTest(name = "{2} - {3} is the first on page {0} with size {1}")
  @MethodSource("contactTypeCodes")
  @DisplayName("List contact type codes")
  void shouldListContactTypes(Integer page, Integer size, String code, String description) {
    Function<UriBuilder, URI> uri = uriBuilder -> {

      UriBuilder localBuilder = uriBuilder
          .path("/api/codes/contactTypes");

      if (page != null) {
        localBuilder = localBuilder.queryParam("page", page);
      }
      if (size != null) {
        localBuilder = localBuilder.queryParam("size", size);
      }

      return localBuilder.build(new HashMap<>());
    };

    client
        .get()
        .uri(uri)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].code").isNotEmpty()
        .jsonPath("$[0].code").isEqualTo(code)
        .jsonPath("$[0].name").isNotEmpty()
        .jsonPath("$[0].name").isEqualTo(description);
  }


  private static Stream<Arguments> contactTypeCodes() {
    return
        Stream.of(
            Arguments.of(null, null, "TC", "BCTS Contractor"),
            Arguments.of(0, 1, "TC", "BCTS Contractor")
        );
  }

}
