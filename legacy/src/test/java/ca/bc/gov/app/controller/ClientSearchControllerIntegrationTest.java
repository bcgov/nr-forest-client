package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.AddressSearchDto;
import ca.bc.gov.app.dto.ContactSearchDto;
import ca.bc.gov.app.exception.MissingRequiredParameterException;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import org.springframework.web.reactive.function.BodyInserters;

@Slf4j
@DisplayName("Integrated Test | Client Search Controller")
class ClientSearchControllerIntegrationTest extends
    AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @ParameterizedTest
  @MethodSource("byEmail")
  @DisplayName("Search someone by email")
  void shouldSearchByMail(
      String email,
      String expected,
      Class<RuntimeException> exception
  ) {
    ResponseSpec response =
        client
            .get()
            .uri(
                uriBuilder -> uriBuilder
                    .path("/api/search/email")
                    .queryParamIfPresent("email", Optional.ofNullable(email))
                    .build()
            )
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .exchange();

    if (StringUtils.isNotBlank(expected)) {
      response
          .expectStatus().isOk()
          .expectBody()
          .jsonPath("$[0].clientNumber").isNotEmpty()
          .jsonPath("$[0].clientNumber").isEqualTo(expected)
          .jsonPath("$[0].clientName").isNotEmpty()
          .consumeWith(System.out::println);
    }

    if (exception != null) {
      response.expectStatus().is4xxClientError();
    }

  }

  @ParameterizedTest
  @MethodSource("byPhone")
  @DisplayName("Search someone by phone")
  void shouldSearchByPhone(
      String phone,
      String expected,
      Class<RuntimeException> exception
  ) {
    ResponseSpec response =
        client
            .get()
            .uri(
                uriBuilder -> uriBuilder
                    .path("/api/search/phone")
                    .queryParamIfPresent("phone", Optional.ofNullable(phone))
                    .build()
            )
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .exchange();

    if (StringUtils.isNotBlank(expected)) {
      response
          .expectStatus().isOk()
          .expectBody()
          .jsonPath("$[0].clientNumber").isNotEmpty()
          .jsonPath("$[0].clientNumber").isEqualTo(expected)
          .jsonPath("$[0].clientName").isNotEmpty()
          .consumeWith(System.out::println);
    }

    if (exception != null) {
      response.expectStatus().is4xxClientError();
    }

  }

  @ParameterizedTest
  @MethodSource("byLocation")
  @DisplayName("Search someone by location")
  void shouldSearchByLocation(
      AddressSearchDto location,
      String expected,
      Class<RuntimeException> exception
  ) {
    ResponseSpec response =
        client
            .post()
            .uri("/api/search/address")
            .body(BodyInserters.fromValue(location))
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .exchange();

    if (StringUtils.isNotBlank(expected)) {
      response
          .expectStatus().isOk()
          .expectBody()
          .jsonPath("$[0].clientNumber").isNotEmpty()
          .jsonPath("$[0].clientNumber").isEqualTo(expected)
          .jsonPath("$[0].clientName").isNotEmpty()
          .consumeWith(System.out::println);
    }

    if (exception != null) {
      response.expectStatus().is4xxClientError();
    }

  }

  @ParameterizedTest
  @MethodSource("byContact")
  @DisplayName("Search someone by contact")
  void shouldSearchByContact(
      ContactSearchDto contact,
      String expected,
      Class<RuntimeException> exception
  ) {
    ResponseSpec response =
        client
            .post()
            .uri("/api/search/contact")
            .body(BodyInserters.fromValue(contact))
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .exchange();

    if (StringUtils.isNotBlank(expected)) {
      response
          .expectStatus().isOk()
          .expectBody()
          .jsonPath("$[0].clientNumber").isNotEmpty()
          .jsonPath("$[0].clientNumber").isEqualTo(expected)
          .jsonPath("$[0].clientName").isNotEmpty()
          .consumeWith(System.out::println);
    }

    if (exception != null) {
      response.expectStatus().is4xxClientError();
    }

  }

  @ParameterizedTest
  @MethodSource("acronym")
  @DisplayName("Search someone by acronym")
  void shouldSearchAcronym(
      String acronym,
      String expected,
      Class<RuntimeException> exception
  ) {

    ResponseSpec response =
        client
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/api/search/acronym")
                    .queryParam("acronym", Optional.ofNullable(acronym))
                    .build(new HashMap<>())
            )
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .exchange();

    if (StringUtils.isNotBlank(expected)) {
      response
          .expectStatus().isOk()
          .expectBody()
          .jsonPath("$[0].clientNumber").isNotEmpty()
          .jsonPath("$[0].clientNumber").isEqualTo(expected)
          .jsonPath("$[0].clientName").isNotEmpty()
          .consumeWith(System.out::println);
    }

    if (exception != null) {
      response.expectStatus().is4xxClientError();
    }

  }

  @ParameterizedTest
  @MethodSource("doingBusinessAs")
  @DisplayName("Search someone by doing business as")
  void shouldSearchDoingBusinessAs(
      String acronym,
      Boolean isFuzzy,
      String expected,
      Class<RuntimeException> exception
  ) {

    ResponseSpec response =
        client
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/api/search/doingBusinessAs")
                    .queryParam("dbaName", Optional.ofNullable(acronym))
                    .queryParamIfPresent("isFuzzy", Optional.ofNullable(isFuzzy))
                    .build(new HashMap<>())
            )
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .exchange();

    if (StringUtils.isNotBlank(expected)) {
      response
          .expectStatus().isOk()
          .expectBody()
          .jsonPath("$[0].clientNumber").isNotEmpty()
          .jsonPath("$[0].clientNumber").isEqualTo(expected)
          .jsonPath("$[0].clientName").isNotEmpty()
          .consumeWith(System.out::println);
    }

    if (exception != null) {
      response.expectStatus().is4xxClientError();
    }

  }

  @ParameterizedTest
  @MethodSource("clientName")
  @DisplayName("Search someone by clientName with full match")
  void shouldSearchClientName(
      String clientName,
      String expected,
      Class<RuntimeException> exception
  ) {

    ResponseSpec response =
        client
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/api/search/clientName")
                    .queryParam("clientName", Optional.ofNullable(clientName))
                    .build(new HashMap<>())
            )
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .exchange();

    if (StringUtils.isNotBlank(expected)) {
      response
          .expectStatus().isOk()
          .expectBody()
          .jsonPath("$[0].clientNumber").isNotEmpty()
          .jsonPath("$[0].clientNumber").isEqualTo(expected)
          .jsonPath("$[0].clientName").isNotEmpty()
          .consumeWith(System.out::println);
    }

    if (exception != null) {
      response.expectStatus().is4xxClientError();
    }

  }

  @ParameterizedTest
  @MethodSource("byPredictive")
  @DisplayName("Search using the predictive search")
  void shouldSearchPredicatively(
      String searchValue,
      Integer page,
      Integer size,
      String expectedClientNumber,
      String expectedClientName
  ) {

    ResponseSpec response =
        client
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/api/search")
                    .queryParamIfPresent("value", Optional.ofNullable(searchValue))
                    .queryParamIfPresent("page", Optional.ofNullable(page))
                    .queryParamIfPresent("size", Optional.ofNullable(size))
                    .build(new HashMap<>())
            )
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .exchange();

    if (StringUtils.isNotBlank(expectedClientNumber)) {
      response
          .expectStatus().isOk()
          .expectHeader()
          .exists("X-Total-Count")
          .expectBody()
          .jsonPath("$[0].clientNumber").isNotEmpty()
          .jsonPath("$[0].clientNumber").isEqualTo(expectedClientNumber)
          .jsonPath("$[0].clientName").isNotEmpty()
          .jsonPath("$[0].name").isEqualTo(expectedClientName)
          .consumeWith(System.out::println);
    } else {
      response.expectStatus().isOk()
          .expectBody()
          .consumeWith(System.out::println).json("[]");
    }

  }

  @Test
  @DisplayName("Search using the predictive search")
  void shouldSearchEmpty() {

    ResponseSpec response =
        client
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/api/search")
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .build(new HashMap<>())
            )
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .exchange();

      response
          .expectStatus().isOk()
          .expectHeader()
          .exists("X-Total-Count")
          .expectBody()
          .jsonPath("$[0].clientNumber").isNotEmpty()
          .jsonPath("$[0].clientName").isNotEmpty()
          .jsonPath("$.length()").isEqualTo(10)
          .consumeWith(System.out::println);

  }

  private static Stream<Arguments> byEmail() {
    return
        Stream.concat(
            emptyCases(),
            Stream
                .of(
                    Arguments.of("celinedion@email.ca", StringUtils.EMPTY, null),
                    Arguments.of("uturfes0@cnn.com", "00000103", null),
                    Arguments.of("jbaxter@mail.ca", "00000002", null)
                )
        );
  }

  private static Stream<Arguments> byPhone() {
    return
        Stream.concat(
            emptyCases(), Stream
                .of(
                    Arguments.of("2502502550", StringUtils.EMPTY, null),
                    Arguments.of("2894837433", "00000103", null)
                )
        );
  }

  private static Stream<Arguments> byLocation() {
    return Stream
        .of(
            Arguments.of(new AddressSearchDto(
                "",
                "",
                "",
                "",
                ""
            ), null, MissingRequiredParameterException.class),
            Arguments.of(new AddressSearchDto(
                "510 FULTON PLAZA",
                "FORT MCMURRAY",
                "AB",
                "T9J9R1",
                ""
            ), null, MissingRequiredParameterException.class),
            Arguments.of(new AddressSearchDto(
                "510 FULTON PLAZA",
                "FORT MCMURRAY",
                "AB",
                "T9J9R1",
                "CANADA"
            ), "00000123", null)
        );
  }

  private static Stream<Arguments> byContact() {
    return Stream
        .of(
            Arguments.of(new ContactSearchDto(
                "",
                "",
                "",
                "",
                "",
                "",
                ""
            ), null, MissingRequiredParameterException.class),
            Arguments.of(new ContactSearchDto(
                "RICARDO",
                "JAMESON",
                "",
                "RBRISLEN5@UN.ORG",
                "7589636074",
                "",
                ""
            ), null, MissingRequiredParameterException.class),
            Arguments.of(new ContactSearchDto(
                "RICARDO",
                "JAMESON",
                "BRISLEN",
                "",
                "7589636074",
                "",
                ""
            ), null, MissingRequiredParameterException.class),
            Arguments.of(new ContactSearchDto(
                "RICARDO",
                "JAMESON",
                "BRISLEN",
                "RBRISLEN5@UN.ORG",
                "",
                "",
                ""
            ), null, MissingRequiredParameterException.class),
            Arguments.of(new ContactSearchDto(
                "RICARDO",
                "",
                "BRISLEN",
                "RBRISLEN5@UN.ORG",
                "7589636074",
                "",
                ""
            ), "00000137", null),
            Arguments.of(new ContactSearchDto(
                "RICARDO",
                "",
                "BRIEN",
                "RBRISLEN5@UN.ORG",
                "7589636074",
                "",
                ""
            ), "00000137", null),
            Arguments.of(new ContactSearchDto(
                "RICARDO",
                null,
                "BRISLEN",
                "RBRISLEN5@UN.ORG",
                "7589636074",
                "",
                ""
            ), "00000137", null),
            Arguments.of(new ContactSearchDto(
                "RICARDO",
                "  ",
                "BRISLEN",
                "RBRISLEN5@UN.ORG",
                "7589636074",
                "",
                ""
            ), "00000137", null),
            Arguments.of(new ContactSearchDto(
                "RICARDO",
                "JAMESON",
                "BRISLEN",
                "RBRISLEN5@UN.ORG",
                "7589636074",
                "",
                ""
            ), null, null),
            Arguments.of(new ContactSearchDto(
                "RANDOLPH",
                null,
                "BRISLEN",
                "RBRISLEN5@UN.ORG",
                "7589636074",
                "",
                ""
            ), null, null)
        );
  }

  private static Stream<Arguments> emptyCases() {
    return Stream
        .of(
            Arguments.of(null, null, MissingRequiredParameterException.class),
            Arguments.of(StringUtils.EMPTY, null, MissingRequiredParameterException.class),
            Arguments.of("  ", null, MissingRequiredParameterException.class)
        );
  }

  private static Stream<Arguments> acronym() {
    return
        Stream.concat(
            emptyCases(),
            Stream
                .of(
                    Arguments.of("SAMPLIBC", "00000004", null),
                    Arguments.of("BCGOV", StringUtils.EMPTY, null)
                )
        );
  }

  private static Stream<Arguments> clientName() {
    return
        Stream.concat(
            emptyCases(),
            Stream
                .of(
                    Arguments.of("DOREEN FOREST PRODUCTS LTD.", "00000013", null),
                    Arguments.of("REICHERT, KILBACK AND EMARD", "00000123", null),
                    Arguments.of("THE MATRIX", StringUtils.EMPTY, null)
                )
        );
  }

  private static Stream<Arguments> doingBusinessAs() {
    return
        Stream
            .of(
                Arguments.of(null, null, null, MissingRequiredParameterException.class),
                Arguments.of(null, false, null, MissingRequiredParameterException.class),
                Arguments.of(null, true, null, MissingRequiredParameterException.class),
                Arguments.of(StringUtils.EMPTY, null, null,
                    MissingRequiredParameterException.class),
                Arguments.of(StringUtils.EMPTY, false, null,
                    MissingRequiredParameterException.class),
                Arguments.of(StringUtils.EMPTY, true, null,
                    MissingRequiredParameterException.class),
                Arguments.of("  ", null, null, MissingRequiredParameterException.class),
                Arguments.of("  ", false, null, MissingRequiredParameterException.class),
                Arguments.of("  ", true, null, MissingRequiredParameterException.class),

                Arguments.of("BORIS AND BORIS INC.", null, "00000003", null),
                Arguments.of("BORIS AND BORIS INC.", false, "00000003", null),
                Arguments.of("BORIS AND BORIS", true, "00000003", null),

                Arguments.of("ELARICHO", null, "00000005", null),
                Arguments.of("ELARICHO", false, "00000005", null),
                Arguments.of("ELARICHO", true, "00000005", null),

                Arguments.of("ELARICO", true, "00000005", null),
                Arguments.of("ELACHO", true, StringUtils.EMPTY, null),
                Arguments.of("ELARICO", false, StringUtils.EMPTY, null)
            );
  }

  private static Stream<Arguments> byPredictive() {
    return Stream
        .of(
            Arguments.of("pollich", null, null, "00000114", "POLLICH-ABERNATHY"),
            Arguments.of("pollich", 0, 2, "00000114", "POLLICH-ABERNATHY"),
            Arguments.of("pollich", 4, 10, StringUtils.EMPTY, StringUtils.EMPTY),
            Arguments.of("kilback", null, null, "00000123", "REICHERT, KILBACK AND EMARD"),
            Arguments.of("kilback", 0, 1, "00000123", "REICHERT, KILBACK AND EMARD"),
            Arguments.of("darbie", null, null, "00000145", "DARBIE BLIND (MINYX)"),
            Arguments.of("darbie", 0, 4, "00000145", "DARBIE BLIND (MINYX)"),
            Arguments.of("pietro", null, null, StringUtils.EMPTY, StringUtils.EMPTY),
            Arguments.of("pietro", 0, 5, StringUtils.EMPTY, StringUtils.EMPTY),
            Arguments.of("pietro", 4, 10, StringUtils.EMPTY, StringUtils.EMPTY)
        );
  }

}
