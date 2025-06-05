package ca.bc.gov.app.controller;

import static ca.bc.gov.app.ApplicationConstants.MDC_USERID;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;

@Slf4j
@DisplayName("Integrated Test | Client Patch Controller")
class ClientPatchControllerIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @ParameterizedTest
  @MethodSource("executePatch")
  @DisplayName("Partially update a client")
  void shouldSendPatchUpdate(
      String clientNumber,
      String partialBody,
      String fieldCheck,
      Object originalFieldValue,
      Object changedFieldValue
  ) {

    if (originalFieldValue != null) {
      checkForClientData(clientNumber)
          .jsonPath(fieldCheck).exists()
          .jsonPath(fieldCheck).isEqualTo(originalFieldValue);
    } else {
      checkForClientData(clientNumber).jsonPath(fieldCheck).doesNotExist();
    }

    client
        .patch()
        .uri("/api/clients/partial/{clientNumber}", clientNumber)
        .header("Content-Type", "application/json-patch+json")
        .header(MDC_USERID, "test-user")
        .bodyValue(partialBody)
        .exchange()
        .expectStatus().isAccepted()
        .expectBody(Void.class)
        .consumeWith(System.out::println);

    if (changedFieldValue != null) {
      checkForClientData(clientNumber)
          .jsonPath(fieldCheck).exists()
          .jsonPath(fieldCheck).isNotEmpty()
          .jsonPath(fieldCheck).isEqualTo(changedFieldValue);
    } else {
      checkForClientData(clientNumber).
          jsonPath(fieldCheck).doesNotExist();
    }

  }

  private @NotNull BodyContentSpec checkForClientData(String clientNumber) {
    return client
        .get()
        .uri("/api/search/clientNumber/{clientNumber}", clientNumber)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .consumeWith(System.out::println);
  }

  public static Stream<Arguments> executePatch() {
    return Stream.of(
        argumentSet(
            "Replace the WorkSafe BC Number",
            "00000012",
            "[{\"op\":\"replace\",\"path\":\"/client/wcbFirmNumber\",\"value\":\"616546\"}]",
            "$.client.wcbFirmNumber",
            null,
            "616546"
        ),
        argumentSet(
            "Replace both WorkSafe BC Number and notes",
            "00000011",
            "[{\"op\":\"replace\",\"path\":\"/client/wcbFirmNumber\",\"value\":\"142536\"},{\"op\":\"replace\",\"path\":\"/client/clientComment\",\"value\":\"This is a nota, yay\"}]",
            "$.client.wcbFirmNumber",
            null,
            "142536"
        ),
        argumentSet(
            "Deactivate client due to bankruptcy",
            "00000103",
            "[{\"op\":\"replace\",\"path\":\"/client/clientStatusCode\",\"value\":\"DAC\"},{\"op\":\"add\",\"path\":\"/reasons/0\",\"value\":{\"field\":\"clientStatusCode\",\"reason\":\"BKR\"}}]",
            "$.client.clientStatusCode",
            "ACT",
            "DAC"
        ),
        argumentSet(
            "Activate client due to correction",
            "00000158",
            "[{\"op\":\"replace\",\"path\":\"/client/clientStatusCode\",\"value\":\"ACT\"},{\"op\":\"add\",\"path\":\"/reasons/0\",\"value\":{\"field\":\"clientStatusCode\",\"reason\":\"CORR\"}}]",
            "$.client.clientStatusCode",
            "DAC",
            "ACT"
        ),
        argumentSet(
            "Replace the notes value from a location",
            "00000157",
            "[{\"op\":\"replace\",\"path\":\"/addresses/00/cliLocnComment\",\"value\": \"This is a homeland note\"}]",
            "$.addresses[0].cliLocnComment",
            null,
            "This is a homeland note"
        ),
        argumentSet(
            "Replace the location name from a location",
            "00000157",
            "[{\"op\":\"replace\",\"path\":\"/addresses/00/clientLocnName\",\"value\": \"HOMELAND\"}]",
            "$.addresses[0].clientLocnName",
            null,
            "HOMELAND"
        ),
        argumentSet(
            "Replace the location phone from a location",
            "00000157",
            "[{\"op\":\"replace\",\"path\":\"/addresses/00/cellPhone\",\"value\": \"4008000001\"}]",
            "$.addresses[0].cellPhone",
            null,
            "4008000001"
        ),
        argumentSet(
            "Add a new location",
            "00000137",
            "[{\"op\":\"add\",\"path\":\"/addresses/null\",\"value\":{\"clientNumber\":\"00000137\",\"clientLocnName\":\"Headquarters\",\"addressOne\":\"2975 Jutland Rd\",\"addressTwo\":\"\",\"addressThree\":null,\"city\":\"Victoria\",\"provinceCode\":\"BC\",\"provinceDesc\":\"British Columbia\",\"postalCode\":\"V8T5J9\",\"countryCode\":\"CA\",\"countryDesc\":\"Canada\",\"businessPhone\":\"\",\"homePhone\":\"\",\"cellPhone\":\"\",\"faxNumber\":\"\",\"emailAddress\":\"\",\"locnExpiredInd\":\"N\",\"cliLocnComment\":\"\"}}]",
            "$.addresses[1].clientLocnName",
            null,
            "HEADQUARTERS"
        ),
        argumentSet(
            "Update contact phone",
            "00000158",
            "[{\"op\":\"replace\",\"path\":\"/contacts/25/businessPhone\",\"value\":\"1234567894\"}]",
            "$.contacts[0].businessPhone",
            "7574522379",
            "1234567894"
        ),
        argumentSet(
            "Update contact name",
            "00000114",
            "[{\"op\":\"replace\",\"path\":\"/contacts/17/contactTypeCode\",\"value\":\"BL\"},{\"op\":\"replace\",\"path\":\"/contacts/17/contactName\",\"value\":\"ANGELO GIANDER\"}]",
            "$.contacts[0].contactName",
            "ANGELO GLANDER",
            "ANGELO GIANDER"
        ),
        argumentSet(
            "Remove contact",
            "00000157",
            "[{\"op\":\"remove\",\"path\":\"/contacts/24\"}]",
            "$.contacts[0].contactName",
            "JACK BEANSTALK",
            null
        ),
        argumentSet(
            "Add a new contact",
            "00000158",
            "[{\"op\":\"add\",\"path\":\"/contacts/null\",\"value\":{\"clientNumber\":\"00000158\",\"locationCodes\":[\"00\"],\"contactName\":\"JAMES Lee-Roy\",\"contactTypeCode\":\"DI\",\"contactTypeDesc\":\"Director\",\"businessPhone\":\"2504447788\",\"secondaryPhone\":\"\",\"faxNumber\":\"\",\"emailAddress\":\"leeroy@oakheritagegroup.ca\"}}]",
            "$.contacts[1].contactName",
            null,
            "JAMES LEE-ROY"
        ),
        argumentSet(
            "Associate contact Albus to location Yard 01",
            "00000159",
            "[{\"op\":\"add\",\"path\":\"/contacts/26/locationCodes/1\",\"value\":\"01\"}]",
            "$.contacts[0].locationCodes[1]",
            null,
            "01"
        ),
        argumentSet(
            "Remove association to the first location code",
            "00000159",
            "[{\"op\":\"remove\",\"path\":\"/contacts/26/locationCodes/0\"}]",
            "$.contacts[0].locationCodes[0]",
            "00",
            "01"
        ),
        argumentSet(
            "Update address information",
            "00000002",
            "[{\"op\":\"replace\",\"path\":\"/addresses/00/city\",\"value\":\"HAMILTON\"},{\"op\":\"add\",\"path\":\"/reasons/0\",\"value\":{\"field\":\"/addresses/00\",\"reason\":\"CORR\"}}]",
            "$.addresses[0].city",
            "VICTORIA",
            "HAMILTON"
        ),
        argumentSet(
            "Update corporate client name with a reason",
            "00000158",
            "[{\"op\":\"replace\",\"path\":\"/client/clientName\",\"value\":\"THE OAK HERITAGE GROUP\"},{\"op\":\"add\",\"path\":\"/reasons/0\",\"value\":{\"field\":\"/client/name\",\"reason\":\"CORR\"}}]",
            "$.client.clientName",
            "OAK HERITAGE GROUP",
            "THE OAK HERITAGE GROUP"
        ),
        argumentSet(
            "Update individual client name with a reason",
            "00000103",
            "[{\"op\":\"replace\",\"path\":\"/client/legalMiddleName\",\"value\":\"WALLACE\"},{\"op\":\"replace\",\"path\":\"/client/legalFirstName\",\"value\":\"LAWRENCE\"},{\"op\":\"replace\",\"path\":\"/client/clientName\",\"value\":\"FISHBORN\"},{\"op\":\"add\",\"path\":\"/reasons/0\",\"value\":{\"field\":\"/client/name\",\"reason\":\"LNAM\"}}]",
            "$.client.legalFirstName",
            "ADELICE",
            "LAWRENCE"
        )
    );
  }

}