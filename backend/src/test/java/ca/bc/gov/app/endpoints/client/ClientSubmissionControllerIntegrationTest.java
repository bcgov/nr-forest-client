package ca.bc.gov.app.endpoints.client;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.dto.client.ClientAddressDto;
import ca.bc.gov.app.dto.client.ClientBusinessInformationDto;
import ca.bc.gov.app.dto.client.ClientBusinessTypeDto;
import ca.bc.gov.app.dto.client.ClientContactDto;
import ca.bc.gov.app.dto.client.ClientLocationDto;
import ca.bc.gov.app.dto.client.ClientSubmissionDto;
import ca.bc.gov.app.dto.client.ClientSubmitterInformationDto;
import ca.bc.gov.app.dto.client.ClientValueTextDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import ca.bc.gov.app.utils.ClientSubmissionAggregator;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@Slf4j
@DisplayName("Integrated Test | FSA Client Submission Controller")
public class ClientSubmissionControllerIntegrationTest
    extends AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10040)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();

  @BeforeEach
  public void init() {
    wireMockExtension.resetAll();
    wireMockExtension
        .stubFor(get(urlPathEqualTo("/business/api/v2/businesses/1234"))
            .willReturn(
                status(200)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(TestConstants.BCREG_DETAIL_OK)
            )
        );
  }

  @Test
  @DisplayName("Submit client data")
  void shouldSubmitClientData() {
    ClientSubmissionDto clientSubmissionDto =
        new ClientSubmissionDto(
            new ClientBusinessTypeDto(new ClientValueTextDto("A", "Association")),
            new ClientBusinessInformationDto(
                "Auric", "Goldfinger", "1964-07-07",
                "1234", "test", "Auric Enterprises"
            ),
            new ClientLocationDto(
                List.of(
                    new ClientAddressDto(
                        "3570 S Las Vegas Blvd",
                        new ClientValueTextDto("US", ""),
                        new ClientValueTextDto("NV", ""),
                        "Las Vegas", "89109",
                        List.of(
                            new ClientContactDto(
                                "LP", "James", "Bond",
                                "987654321", "bond_james_bond@007.com"
                            )
                        )
                    )
                )
            ),
            new ClientSubmitterInformationDto(
                "James", "Bond", "1234567890",
                "james_bond@MI6.com"
            )
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

  @DisplayName("Fail Validation")
  @ParameterizedTest
  @CsvFileSource(resources = "/failValidationTest.csv", numLinesToSkip = 1)
  void shouldFailValidationSubmit(
      @AggregateWith(ClientSubmissionAggregator.class) ClientSubmissionDto clientSubmissionDto) {
    System.out.println(clientSubmissionDto);
    client
        .post()
        .uri("/api/clients/submissions")
        .body(Mono.just(clientSubmissionDto), ClientSubmissionDto.class)
        .exchange()
        .expectStatus().isBadRequest()
        .expectHeader().valueEquals("Reason", "Validation failed");
  }
}
