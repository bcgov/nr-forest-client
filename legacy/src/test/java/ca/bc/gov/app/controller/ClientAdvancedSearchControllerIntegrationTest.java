package ca.bc.gov.app.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

@Slf4j
@DisplayName("Integrated Test | Client Advanced Search Controller")
class ClientAdvancedSearchControllerIntegrationTest extends
    AbstractTestContainerIntegrationTest {

  private static final String BASE_PATH = "/api/clients/advanced-search";

  @Test
  @DisplayName("Should return latest entries when no search criteria provided")
  void shouldReturnLatestEntriesWhenNoCriteria() {
    client
        .get()
        .uri(uriBuilder -> uriBuilder
            .path(BASE_PATH)
            .queryParam("page", 0)
            .queryParam("size", 10)
            .build()
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectHeader()
        .value("x-total-count",
            count -> assertThat(Integer.parseInt(count)).isGreaterThanOrEqualTo(0))
        .expectBody()
        .consumeWith(System.out::println);
  }

  @Test
  @DisplayName("Should return latest entries when only blank criteria provided")
  void shouldReturnLatestEntriesWhenBlankCriteria() {
    client
        .get()
        .uri(uriBuilder -> uriBuilder
            .path(BASE_PATH)
            .queryParam("clientName", " ")
            .queryParam("firstName", "")
            .queryParam("page", 0)
            .queryParam("size", 5)
            .build()
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectHeader()
        .value("x-total-count",
            count -> assertThat(Integer.parseInt(count)).isGreaterThanOrEqualTo(0))
        .expectBody()
        .consumeWith(System.out::println);
  }

  @ParameterizedTest
  @MethodSource("singleFieldSearches")
  @DisplayName("Should find clients by a single search field")
  void shouldFindBySingleField(
      String paramName,
      String paramValue,
      String expectedClientNumber
  ) {
    ResponseSpec response = client
        .get()
        .uri(uriBuilder -> uriBuilder
            .path(BASE_PATH)
            .queryParam(paramName, paramValue)
            .queryParam("page", 0)
            .queryParam("size", 10)
            .build()
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange();

    if (StringUtils.isNotBlank(expectedClientNumber)) {
      response
          .expectStatus().isOk()
          .expectHeader()
          .value("x-total-count",
              count -> assertThat(Integer.parseInt(count)).isGreaterThan(0))
          .expectBody()
          .jsonPath("$[?(@.clientNumber == '" + expectedClientNumber + "')]").exists()
          .consumeWith(System.out::println);
    } else {
      response
          .expectStatus().isOk()
          .expectBody()
          .consumeWith(System.out::println);
    }
  }

  @ParameterizedTest
  @MethodSource("multiFieldSearches")
  @DisplayName("Should find clients by multiple search fields combined with AND")
  void shouldFindByMultipleFields(
      String clientName,
      String firstName,
      String clientStatus,
      String clientType,
      String emailAddress,
      String contactName,
      String expectedClientNumber
  ) {
    ResponseSpec response = client
        .get()
        .uri(uriBuilder -> {
          uriBuilder.path(BASE_PATH);
          if (clientName != null) uriBuilder.queryParam("clientName", clientName);
          if (firstName != null) uriBuilder.queryParam("firstName", firstName);
          if (clientStatus != null) uriBuilder.queryParam("clientStatus", clientStatus);
          if (clientType != null) uriBuilder.queryParam("clientType", clientType);
          if (emailAddress != null) uriBuilder.queryParam("emailAddress", emailAddress);
          if (contactName != null) uriBuilder.queryParam("contactName", contactName);
          uriBuilder.queryParam("page", 0);
          uriBuilder.queryParam("size", 10);
          return uriBuilder.build();
        })
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange();

    if (StringUtils.isNotBlank(expectedClientNumber)) {
      response
          .expectStatus().isOk()
          .expectHeader()
          .value("x-total-count",
              count -> assertThat(Integer.parseInt(count)).isGreaterThan(0))
          .expectBody()
          .jsonPath("$[?(@.clientNumber == '" + expectedClientNumber + "')]").exists()
          .consumeWith(System.out::println);
    } else {
      response
          .expectStatus().isOk()
          .expectHeader()
          .value("x-total-count", count -> assertThat(count).isEqualTo("0"))
          .expectBody()
          .jsonPath("$").isEmpty()
          .consumeWith(System.out::println);
    }
  }

  @ParameterizedTest
  @MethodSource("commaSeparatedSearches")
  @DisplayName("Should find clients using comma-separated values for status, type, and idType")
  void shouldFindByCommaSeparatedValues(
      String paramName,
      String paramValue,
      String expectedClientNumber
  ) {
    ResponseSpec response = client
        .get()
        .uri(uriBuilder -> uriBuilder
            .path(BASE_PATH)
            .queryParam(paramName, paramValue)
            .queryParam("page", 0)
            .queryParam("size", 10)
            .build()
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange();

    if (StringUtils.isNotBlank(expectedClientNumber)) {
      response
          .expectStatus().isOk()
          .expectHeader()
          .value("x-total-count",
              count -> assertThat(Integer.parseInt(count)).isGreaterThan(0))
          .expectBody()
          .jsonPath("$[?(@.clientNumber == '" + expectedClientNumber + "')]").exists()
          .consumeWith(System.out::println);
    } else {
      response
          .expectStatus().isOk()
          .expectBody()
          .consumeWith(System.out::println);
    }
  }

  @Test
  @DisplayName("Should return empty result when no clients match the criteria")
  void shouldReturnEmptyWhenNoMatch() {
    client
        .get()
        .uri(uriBuilder -> uriBuilder
            .path(BASE_PATH)
            .queryParam("clientName", "ZZZZNONEXISTENT")
            .queryParam("page", 0)
            .queryParam("size", 10)
            .build()
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$").isEmpty()
        .consumeWith(System.out::println);
  }

  @Test
  @DisplayName("Should respect pagination parameters")
  void shouldRespectPagination() {
    // Search for all active clients (many results), but only get 2 per page
    client
        .get()
        .uri(uriBuilder -> uriBuilder
            .path(BASE_PATH)
            .queryParam("clientStatus", "ACT")
            .queryParam("page", 0)
            .queryParam("size", 2)
            .build()
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectHeader()
        .value("x-total-count",
            count -> assertThat(Integer.parseInt(count)).isGreaterThan(2))
        .expectBody()
        .jsonPath("$.length()").isEqualTo(2)
        .consumeWith(System.out::println);
  }

  @Test
  @DisplayName("Should find client by email address matching location email")
  void shouldFindByLocationEmail() {
    // 00000138 has location email: egirault0@zdnet.com
    client
        .get()
        .uri(uriBuilder -> uriBuilder
            .path(BASE_PATH)
            .queryParam("emailAddress", "egirault0@zdnet.com")
            .queryParam("page", 0)
            .queryParam("size", 10)
            .build()
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectHeader()
        .value("x-total-count",
            count -> assertThat(Integer.parseInt(count)).isGreaterThan(0))
        .expectBody()
        .jsonPath("$[0].clientNumber").isEqualTo("00000138")
        .consumeWith(System.out::println);
  }

  @Test
  @DisplayName("Should find client by email address matching contact email")
  void shouldFindByContactEmail() {
    // 00000137 has contact email: RBRISLEN5@UN.ORG
    client
        .get()
        .uri(uriBuilder -> uriBuilder
            .path(BASE_PATH)
            .queryParam("emailAddress", "RBRISLEN5@UN.ORG")
            .queryParam("page", 0)
            .queryParam("size", 10)
            .build()
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectHeader()
        .value("x-total-count",
            count -> assertThat(Integer.parseInt(count)).isGreaterThan(0))
        .expectBody()
        .jsonPath("$[?(@.clientNumber == '00000137')]").exists()
        .consumeWith(System.out::println);
  }

  @Test
  @DisplayName("Should return 400 Bad Request for invalid updatedToDate parameter")
  void shouldReturnBadRequestForInvalidUpdatedToDate() {
    client
        .get()
        .uri(uriBuilder -> uriBuilder
            .path(BASE_PATH)
            .queryParam("updatedFromDate", "2026-04-01")
            .queryParam("updatedToDate", "2026-04-34")
            .build()
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody(String.class)
        .value(msg -> assertThat(msg)
            .contains("Invalid value")
            .contains("updatedToDate")
            .contains("Type mismatch"));
  }

  private static Stream<Arguments> singleFieldSearches() {
    return Stream.of(
        // Search by clientName
        Arguments.of("clientName", "JERROM", "00000138"),
        Arguments.of("clientName", "POLLICH-ABERNATHY", "00000114"),
        Arguments.of("clientName", "ZZZZNONEXISTENT", StringUtils.EMPTY),

        // Search by firstName
        Arguments.of("firstName", "DARBIE", "00000145"),
        Arguments.of("firstName", "MATELDA", "00000137"),
        Arguments.of("firstName", "ZZZZNONEXISTENT", StringUtils.EMPTY),

        // Search by clientStatus (single value)
        Arguments.of("clientStatus", "ACT", "00000001"),

        // Search by clientType
        Arguments.of("clientType", "I", "00000001"),
        Arguments.of("clientType", "C", "00000101"),

        // Search by clientIdType
        Arguments.of("clientIdType", "BCRE", "00000103"),
        Arguments.of("clientIdType", "ABDL", "00000171"),

        // Search by contactName
        Arguments.of("contactName", "RICARDO BRISLEN", "00000137"),
        Arguments.of("contactName", "UDALL TURFES", "00000103")
    );
  }

  private static Stream<Arguments> multiFieldSearches() {
    return Stream.of(
        // clientStatus=ACT + firstName=MATELDA → 00000137
        Arguments.of(null, "MATELDA", "ACT", null, null, null, "00000137"),

        // clientType=I + firstName=DARBIE → 00000145
        Arguments.of(null, "DARBIE", null, "I", null, null, "00000145"),

        // clientName=JERROM + clientType=C → 00000138
        Arguments.of("JERROM", null, null, "C", null, null, "00000138"),

        // contactName + clientStatus → 00000137
        Arguments.of(null, null, "ACT", null, null, "RICARDO BRISLEN", "00000137"),

        // No match: clientName=JERROM + clientType=I (JERROM is type C)
        Arguments.of("JERROM", null, null, "I", null, null, StringUtils.EMPTY),

        // No match: firstName=DARBIE + clientName=LINDHE (different clients)
        Arguments.of("LINDHE", "DARBIE", null, null, null, null, StringUtils.EMPTY)
    );
  }

  private static Stream<Arguments> commaSeparatedSearches() {
    return Stream.of(
        // Comma-separated clientStatus: ACT,DAC — should find ACT clients
        Arguments.of("clientStatus", "ACT,DAC", "00000001"),

        // Comma-separated clientType: I,C — should find both individual and company clients
        Arguments.of("clientType", "I,C", "00000001"),

        // Comma-separated clientIdType: BCRE,ABDL — should find 00000103 (BCRE) and 00000171 (ABDL)
        Arguments.of("clientIdType", "BCRE,ABDL", "00000103"),

        // Single value still works
        Arguments.of("clientStatus", "ACT", "00000001"),

        // Non-matching comma-separated — no data with these statuses
        Arguments.of("clientStatus", "DEC,REC", StringUtils.EMPTY)
    );
  }
  
}