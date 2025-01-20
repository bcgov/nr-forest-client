package ca.bc.gov.app.controller;


import ca.bc.gov.app.dto.ForestClientContactDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.reactive.function.BodyInserters;

@Slf4j
@DisplayName("Integrated Test | Client Contact Controller")
class ClientContactControllerIntegrationTest extends
    AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @ParameterizedTest
  @MethodSource("saveContact")
  @DisplayName("Save a contact")
  void shouldSaveLocation(String clientNumber) {
    client
        .post()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/contacts")
                .build(Map.of())
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .body(BodyInserters.fromValue(
                new ForestClientContactDto(
                    "1",
                    clientNumber,
                    "00",
                    List.of("00"),
                    "BL",
                    "James Baxter",
                    "2502502555",
                    StringUtils.EMPTY,
                    StringUtils.EMPTY,
                    "oknowwhat@mail.ca",
                    "Test",
                    "Test",
                    1L
                )
            )
        )
        .exchange()
        .expectStatus().isCreated()
        .expectBody(String.class)
        .equals(clientNumber);
  }

  @ParameterizedTest
  @MethodSource("search")
  @DisplayName("Search a contact")
  void shouldSearchContact(
      String firstName,
      String lastName,
      String email,
      String phone,
      Integer expected
  ) {
    client
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/api/contacts/search")
                .queryParam("firstName", firstName)
                .queryParam("lastName", lastName)
                .queryParam("email", email)
                .queryParam("phone", phone)
                .build(Map.of())
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBodyList(ForestClientContactDto.class)
        .hasSize(expected);
  }

  private static Stream<String> saveContact() {
    return Stream.of("00000001", "00000002", "00000003");
  }

  private static Stream<Arguments> search() {
    return Stream.of(
        Arguments.of("Jason", "Momoa", "myman@momoa.ca", "6046666735", 2),
        Arguments.of("James", "Baxter", "jbaxter@mail.ca", "6046666755", 3),
        Arguments.of("Nedad", "Kontic", "konticboss@kelpic.ca", "6046646755", 1),
        Arguments.of("Jack", "Ryan", "ryan.jack@tomclancy.ca", "6046666735", 2),
        Arguments.of("Jack", "Ryan", "ryan.jack@tomclancy.ca", "2502502555", 2),
        Arguments.of("Domingos", "Chaves", "dingo@tomclancy.ca", "2554457789", 0)
    );
  }

}