package ca.bc.gov.app.controller;

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

        )
    );
  }

}