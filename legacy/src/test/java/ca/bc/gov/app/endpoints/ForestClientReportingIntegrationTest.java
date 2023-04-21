package ca.bc.gov.app.endpoints;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import ca.bc.gov.app.service.ReportingClientService;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@DisplayName("Integrated Test | Forest Client Reporting Controller")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ForestClientReportingIntegrationTest extends
    AbstractTestContainerIntegrationTest {

  private String reportId = null;

  @Autowired
  protected WebTestClient client;

  @Autowired
  private ReportingClientService service;

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10001)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();

  @BeforeAll
  public static void setUp() throws IOException {
    Path tempFolder = Paths.get("./temp");

    if(!tempFolder.toFile().exists())
      tempFolder.toFile().mkdirs();

    Files
        .list(tempFolder)
        .forEach(path -> path.toFile().delete());

    tempFolder.toFile().delete();

    wireMockExtension.resetAll();
  }

  @Test
  @DisplayName("List no reports available")
  @Order(1)
  void shouldGetNoReportsListed() throws IOException {

    client
        .get()
        .uri("/api/reports")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .json("[]");
  }

  @Test
  @DisplayName("Fail when getting a dummy report")
  @Order(2)
  void shouldFailWhenGettingReportNonExisting() {
    client
        .get()
        .uri("/api/reports/ad8b3785-9c88-4378-aca3-ba93a4c3d3fa")
        .exchange()
        .expectStatus().isNotFound()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .expectBody(String.class)
        .isEqualTo("No report found for ID ad8b3785-9c88-4378-aca3-ba93a4c3d3fa");

  }

  @Test
  @DisplayName("Generate all spreadsheet")
  @Order(3)
  void shouldGenerateAllSheet() throws IOException {
    wireMockExtension
        .stubFor(
            get(urlPathEqualTo("/v4/search/topic"))
                .withQueryParam("format", equalTo("json"))
                .withQueryParam("inactive", equalTo("any"))
                .withQueryParam("ordering", equalTo("-score"))
                .withQueryParam("q", matching("^(.*)$"))
                .willReturn(
                    aResponse()
                        .withBody(TestConstants.ORGBOOK_TOPIC)
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withTransformers("response-template")
                )
        );

    service.generateAllClientsReport();

    assertTrue(
        Files
            .list(Paths.get("./temp"))
            .findFirst()
            .isPresent()
    );
  }

  @Test
  @DisplayName("List Reports available")
  @Order(4)
  void shouldGetListOfReports() {
    client
        .get()
        .uri("/api/reports")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0]").isNotEmpty()
        .consumeWith(response -> persistIds(response.getResponseBody()));

  }


  @Test
  @DisplayName("Get the report")
  @Order(5)
  void shouldGetTheReport() throws IOException {

    Optional<String> reportId =
    Files
        .list(Paths.get("./temp"))
        .map(path -> path.toFile().getName())
        .map(name -> name.replace(".xlsx",StringUtils.EMPTY))
        .findFirst();

    assertTrue(reportId.isPresent());

    client
        .get()
        .uri("/api/reports/"+reportId.get())
        .exchange()
        .expectStatus().isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .expectHeader().valueEquals(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=FC Report "+reportId.get()+".xlsx");
  }

  @Test
  @DisplayName("Remove the report")
  @Order(6)
  void shouldRemoveReport() throws IOException {
    Optional<String> reportId =
        Files
            .list(Paths.get("./temp"))
            .map(path -> path.toFile().getName())
            .map(name -> name.replace(".xlsx",StringUtils.EMPTY))
            .findFirst();

    assertTrue(reportId.isPresent());

    client
        .delete()
        .uri("/api/reports/"+reportId.get())
        .exchange()
        .expectStatus().isAccepted()
        .expectBody()
        .isEmpty();
  }

  @Test
  @DisplayName("Remove the report")
  @Order(7)
  void shouldHaveNoReportAgain() throws IOException {
    shouldGetNoReportsListed();
  }

  private void persistIds(byte[] data) {
    this.reportId =
        new String(data, Charset.defaultCharset())
            .replace("[", StringUtils.EMPTY)
            .replace("]", StringUtils.EMPTY)
            .replace("\"", StringUtils.EMPTY);

    System.out.printf("Report Id loaded " + this.reportId);

    assertNotNull(this.reportId);
  }
}
