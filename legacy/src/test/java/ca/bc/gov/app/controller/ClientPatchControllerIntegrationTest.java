package ca.bc.gov.app.controller;

import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;

@Slf4j
@DisplayName("Integrated Test | Client Patch Controller")
class ClientPatchControllerIntegrationTest extends
    AbstractTestContainerIntegrationTest {


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

    BodyContentSpec preCheck =
    client
        .get()
        .uri("/api/search/clientNumber/{clientNumber}", clientNumber)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .consumeWith(System.out::println);

    if(originalFieldValue != null) {
      preCheck
          .jsonPath(fieldCheck).exists()
          .jsonPath(fieldCheck).isEqualTo(originalFieldValue);
    } else {
      preCheck.jsonPath(fieldCheck).doesNotExist();
    }

    client
        .patch()
        .uri("/api/clients/partial/{clientNumber}", clientNumber)
        .header("Content-Type", "application/json-patch+json")
        .bodyValue(partialBody)
        .exchange()
        .expectStatus().isAccepted()
        .expectBody(Void.class);

    BodyContentSpec postCheck =
    client
        .get()
        .uri("/api/search/clientNumber/{clientNumber}", clientNumber)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .consumeWith(System.out::println);

    if(changedFieldValue != null) {
      postCheck
          .jsonPath(fieldCheck).exists()
          .jsonPath(fieldCheck).isNotEmpty()
          .jsonPath(fieldCheck).isEqualTo(changedFieldValue);
    } else {
      postCheck.
          jsonPath(fieldCheck).doesNotExist();
    }

  }

  public static Stream<Arguments> executePatch() {
    return Stream.of(
        Arguments.of(
            "00000138",
            "[{ \"op\":\"replace\", \"path\": \"/contacts/4/contactName\", \"value\": \"ELLISSA J. MCILVORAY\" }]",
            "$.contacts[0].contactName",
            "ELLISSA MCILVORAY",
            "ELLISSA J. MCILVORAY"
        ),
        Arguments.of(
            "00000171",
            "[{ \"op\":\"replace\", \"path\": \"/doingBusinessAs/5/doingBusinessAsName\", \"value\": \"HAUCK WORKS LTD\" }]",
            "$.doingBusinessAs[0].doingBusinessAsName",
            "BLOGSPAN",
            "HAUCK WORKS LTD"
        ),
        Arguments.of(
            "00000117",
            "[{ \"op\":\"remove\", \"path\": \"/contacts/7\"}]",
            "$.contacts[0].contactName",
            "EBENESER STOWER",
            "ANDY EDLAND"
        )
        ,
        Arguments.of(
            "00000117",
            "[{ \"op\":\"add\", \"path\": \"/contacts/2\", \"value\": {\"clientNumber\":\"00000117\",\"clientLocnCode\":\"00\",\"locationCode\":[\"00\"],\"contactCode\":\"DI\",\"contactName\":\"MORGAN FREEMAN\",\"businessPhone\":\"1234567890\",\"secondaryPhone\":null,\"faxNumber\":null,\"emailAddress\":\"morgan@sameold.ca\",\"createdBy\":\"IDIR\\\\PCRUZ\",\"updatedBy\":\"IDIR\\\\PCRUZ\",\"orgUnit\":70}}]",
            "$.contacts[2].contactName",
            null,
            "MORGAN FREEMAN"
        ),
        Arguments.of(
            "00000117",
            "[{ \"op\":\"replace\", \"path\": \"/contacts/8/locationCode\", \"value\": [\"00\",\"01\"]}]",
            "$.contacts[0].locationCode",
            "00",
            List.of("00", "01")
        ),
        Arguments.of(
            "00000145",
            "[{ \"op\":\"add\", \"path\": \"/addresses/1\", \"value\": {\"clientNumber\":\"00000145\",\"clientLocnCode\":\"01\",\"clientLocnName\":\"HOME ADDRESS\",\"addressOne\":\"823 GEORGIA DR\",\"addressTwo\":null,\"addressThree\":null,\"city\":\"CAMPBELL RIVER\",\"province\":\"BC\",\"postalCode\":\"V9H1S2\",\"country\":\"CANADA\",\"businessPhone\":null,\"homePhone\":null,\"cellPhone\":null,\"faxNumber\":null,\"emailAddress\":null,\"locnExpiredInd\":\"N\",\"returnedMailDate\":null,\"trustLocationInd\":\"N\",\"cliLocnComment\":null,\"createdBy\":\"IDIR\\\\PCRUZ\",\"updatedBy\":\"IDIR\\\\PCRUZ\",\"orgUnit\":1}}]",
            "$.addresses[1].clientLocnCode",
            null,
            "01"
        ),
        Arguments.of(
            "00000145",
            "[{ \"op\":\"replace\", \"path\": \"/addresses/19/clientLocnName\", \"value\": \"SOME ADDRESS\" }]",
            "$.addresses[0].clientLocnName",
            "HOME ADDRESS",
            "SOME ADDRESS"
        )
    );
  }

}
