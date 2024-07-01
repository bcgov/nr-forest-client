package ca.bc.gov.app.controller;

import ca.bc.gov.app.dto.AddressSearchDto;
import ca.bc.gov.app.exception.MissingRequiredParameterException;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
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

  private static Stream<Arguments> byEmail() {
    return
        Stream.concat(
            emptyCases(),
            Stream
                .of(
                    Arguments.of("celinedion@email.ca", StringUtils.EMPTY, null),
                    Arguments.of("uturfes0@cnn.com", "00000103", null),
                    Arguments.of("mail@mail.ca", "00000001", null)
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

  private static Stream<Arguments> emptyCases() {
    return Stream
        .of(
            Arguments.of(null, null, MissingRequiredParameterException.class),
            Arguments.of(StringUtils.EMPTY, null, MissingRequiredParameterException.class),
            Arguments.of("  ", null, MissingRequiredParameterException.class)
        );
  }

}
