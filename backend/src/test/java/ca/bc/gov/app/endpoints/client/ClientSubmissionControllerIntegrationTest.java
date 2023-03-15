package ca.bc.gov.app.endpoints.client;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientBusinessTypeDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.ClientSubmitterInformationDto;
import ca.bc.gov.app.dto.client.ClientTypeDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@Slf4j
@DisplayName("Integrated Test | FSA Client Submission Controller")
public class ClientSubmissionControllerIntegrationTest
    extends AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @Test
  @DisplayName("Submit client data")
  void shouldSubmitClientData() {
    ClientSubmissionDto clientSubmissionDto =
        new ClientSubmissionDto(
            new ClientBusinessTypeDto(new ClientTypeDto("A", "Association")),
            new ClientBusinessInformationDto(
                "Auric", "", "1964-07-07",
                "1234", "test", "Auric Enterprises"),
            new ClientLocationDto(
                List.of(
                    new ClientAddressDto(
                        "3570 S Las Vegas Blvd", "US", "NV",
                        "Las Vegas", "89109", 0,
                        List.of(
                            new ClientContactDto(
                                "LP", "James", "007",
                                "987654321", "bond_james_bond@007.com",
                                0))))),
            new ClientSubmitterInformationDto(
                "James", "Bond", "1234567890",
                "james_bond@MI6.com")
        );
    client
        .post()
        .uri("/api/clients/submissions")
        .body(Mono.just(clientSubmissionDto), ClientSubmissionDto.class)
        .exchange()
        .expectStatus().isCreated()
        .expectHeader().location("/api/clients/submissions/1")
        .expectHeader().valueEquals("x-sub-id", "1")
        .expectBody().isEmpty();
  }
}
