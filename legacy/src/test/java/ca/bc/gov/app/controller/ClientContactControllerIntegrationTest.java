package ca.bc.gov.app.controller;


import static ca.bc.gov.app.ApplicationConstants.MDC_USERID;
import ca.bc.gov.app.dto.ForestClientContactDto;
import ca.bc.gov.app.entity.ForestClientContactEntity;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.repository.ForestClientContactRepository;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

@Slf4j
@DisplayName("Integrated Test | Client Contact Controller")
class ClientContactControllerIntegrationTest extends
    AbstractTestContainerIntegrationTest {

  private static final String CLIENT_NUMBER = "00000160";

  @Autowired
  private ForestClientContactRepository forestClientContactRepository;
  
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
  
  @Test
  @DisplayName("Should report a contact as in use when referenced by SCALE_SITE_CONTACT")
  void shouldReportContactInUse() {
    Long contactId = findContactIdByName("IN USE CONTACT");

    client
        .get()
        .uri("/api/contacts/{contactId}/in-use", contactId)
        .exchange()
        .expectStatus().isOk()
        .expectBody(Boolean.class)
        .isEqualTo(true);
  }

  @Test
  @DisplayName("Should report a contact as not in use when there is no SCALE_SITE_CONTACT record")
  void shouldReportContactNotInUse() {
    Long contactId = findContactIdByName("FREE CONTACT");

    client
        .get()
        .uri("/api/contacts/{contactId}/in-use", contactId)
        .exchange()
        .expectStatus().isOk()
        .expectBody(Boolean.class)
        .isEqualTo(false);
  }

  @Test
  @DisplayName("Should prevent deleting a contact that is in use by another system")
  void shouldPreventDeletingContactInUse() {
    Long contactId = findContactIdByName("IN USE CONTACT");

    client
        .patch()
        .uri("/api/clients/partial/{clientNumber}", CLIENT_NUMBER)
        .header("Content-Type", "application/json-patch+json")
        .header(MDC_USERID, "test-user")
        .bodyValue(
            "[{\"op\":\"remove\",\"path\":\"/contacts/" + contactId + "\"}]"
        )
        .exchange()
        .expectStatus().isEqualTo(409)
        .expectBody(String.class)
        .isEqualTo(
            "You can't delete this contact yet because it's being used by EMS, GAS2, LEXIS, "
                + "or SCS. Remove it from the other system first, then try again."
        );
  }

  @Test
  @DisplayName("Should allow deleting a contact that is not in use by another system")
  void shouldAllowDeletingContactNotInUse() {
    Long contactId = findContactIdByName("DELETABLE CONTACT");

    client
        .patch()
        .uri("/api/clients/partial/{clientNumber}", CLIENT_NUMBER)
        .header("Content-Type", "application/json-patch+json")
        .header(MDC_USERID, "test-user")
        .bodyValue(
            "[{\"op\":\"remove\",\"path\":\"/contacts/" + contactId + "\"}]"
        )
        .exchange()
        .expectStatus().isAccepted();
  }

  private Long findContactIdByName(String contactName) {
    return
        forestClientContactRepository
            .findAllByClientNumber(CLIENT_NUMBER)
            .filter(entity -> contactName.equals(entity.getContactName()))
            .next()
            .map(ForestClientContactEntity::getClientContactId)
            .switchIfEmpty(
                Mono.error(new IllegalStateException(
                    "No contact found with name " + contactName
                ))
            )
            .block();
  }

}