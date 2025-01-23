package ca.bc.gov.app.service.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import ca.bc.gov.app.dto.client.ClientListDto;
import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.legacy.AddressSearchDto;
import ca.bc.gov.app.dto.legacy.ContactSearchDto;
import ca.bc.gov.app.dto.legacy.ForestClientDetailsDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;
import reactor.test.StepVerifier;

@DisplayName("Integration Test | Client Legacy Service Test")
class ClientLegacyServiceIntegrationTest extends AbstractTestContainerIntegrationTest {

  @RegisterExtension
  static WireMockExtension legacyStub = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10060)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();

  @Autowired
  private ClientLegacyService service;

  @BeforeEach
  public void setUp() {
    legacyStub.resetAll();
  }

  @DisplayName("searching legacy with userid instead")
  @Test
  void shouldSearchLegacyWhenNoRegistration() {

    String userId = UUID.randomUUID().toString();

    legacyStub
        .stubFor(
            get(urlPathEqualTo("/api/search/registrationOrName"))
                .withQueryParam("registrationNumber", equalTo(userId))
                .willReturn(okJson("[{\"clientNumber\":\"00000001\"}]"))
        );

    service
        .searchLegacy(
            null,
            null,
            userId,
            null
        )
        .as(StepVerifier::create)
        .assertNext(results -> assertEquals("00000001", results.clientNumber()))
        .verifyComplete();

  }

  @ParameterizedTest
  @MethodSource("invalidValues")
  @DisplayName("searching legacy with invalid values")
  void shouldSearchGenericWithInvalidValues(String values) {
    service.searchGeneric("email",values)
        .as(StepVerifier::create)
        .verifyComplete();

  }

  @ParameterizedTest
  @MethodSource("invalidValuesForMap")
  @DisplayName("searching legacy with invalid values for map")
  void shouldNotSearchWhenInvalidCasesHitGeneric(Map<String, List<String>> parameters) {
    service.searchGeneric("generic",parameters)
        .as(StepVerifier::create)
        .verifyComplete();
  }

  @Test
  @DisplayName("searching legacy for location")
  void shouldSearchALocation() {

    legacyStub
        .stubFor(
            post(urlPathEqualTo("/api/search/address"))
                .willReturn(okJson("[{\"clientNumber\":\"00000001\"}]"))
        );

    service.searchLocation(new AddressSearchDto("2975 Jutland Rd","Victoria","BC","V8T5J9","Canada"))
        .as(StepVerifier::create)
        .assertNext(results -> assertEquals("00000001", results.clientNumber()))
        .verifyComplete();
  }

  @Test
  @DisplayName("searching legacy for contact")
  void shouldSearchAContact() {
    legacyStub
        .stubFor(
            post(urlPathEqualTo("/api/search/contact"))
                .willReturn(okJson("[{\"clientNumber\":\"00000001\"}]"))
        );

    service.searchContact(new ContactSearchDto("John",null,"Smith","mail@mail.ca","2505555555","2505555555","2505555555"))
        .as(StepVerifier::create)
        .assertNext(results -> assertEquals("00000001", results.clientNumber()))
        .verifyComplete();
  }


  private static Stream<String> invalidValues() {
    return Stream.of(
        null,
        StringUtils.EMPTY,
        "   "
    );
  }

  private static Stream<Map<String,List<String>>> invalidValuesForMap() {
    return Stream.of(
        Map.of("email",List.of("")),
        Map.of("email",List.of("   ")),
        Map.of("email",List.of()),
        Map.of("",List.of()),
        Map.of("  ",List.of())
    );
  }
  
  @Test
  @DisplayName("searching legacy by client number")
  void shouldSearchLegacyByClientNumber() {
      String clientNumber = "00000001";

      ForestClientDetailsDto expectedDto = new ForestClientDetailsDto(
          clientNumber,
          "MY COMPANY LTD.",
          null,
          null,
          "ACT",
          "Active",
          "C",
          "Corporation",
          null,
          null,
          null,
          "BC",
          "9607514",
          null,
          "678",
          "THIS TEST",
          null,
          null,
          "Y",
          null,
          null,
          null,
          null
      );

      legacyStub
          .stubFor(
              get(urlPathEqualTo("/api/search/clientNumber/" + clientNumber))
                  .willReturn(okJson("{"
                      + "\"clientNumber\":\"00000001\","
                      + "\"clientName\":\"MY COMPANY LTD.\","
                      + "\"legalFirstName\":null,"
                      + "\"legalMiddleName\":null,"
                      + "\"clientStatusCode\":\"ACT\","
                      + "\"clientStatusDesc\":\"Active\","
                      + "\"clientTypeCode\":\"C\","
                      + "\"clientTypeDesc\":\"Corporation\","
                      + "\"clientIdTypeCode\":null,"
                      + "\"clientIdTypeDesc\":null,"
                      + "\"clientIdentification\":null,"
                      + "\"registryCompanyTypeCode\":\"BC\","
                      + "\"corpRegnNmbr\":\"9607514\","
                      + "\"clientAcronym\":null,"
                      + "\"wcbFirmNumber\":\"678\","
                      + "\"ocgSupplierNmbr\":null,"
                      + "\"clientComment\":\"THIS TEST\","
                      + "\"clientCommentUpdateDate\":null,"
                      + "\"clientCommentUpdateUser\":null,"
                      + "\"goodStandingInd\":\"Y\","
                      + "\"birthdate\":null,"
                      + "\"addresses\":null,"
                      + "\"contacts\":null,"
                      + "\"doingBusinessAs\":null"
                      + "}"))
                  .withHeader("Content-Type", equalTo("application/json"))
          );

      service.searchByClientNumber(clientNumber)
          .as(StepVerifier::create)
          .assertNext(clientDetailsDto -> {
              assertThat(clientDetailsDto)
                  .extracting(
                      ForestClientDetailsDto::clientNumber,
                      ForestClientDetailsDto::clientName,
                      ForestClientDetailsDto::clientStatusCode,
                      ForestClientDetailsDto::clientStatusDesc,
                      ForestClientDetailsDto::clientTypeCode,
                      ForestClientDetailsDto::clientTypeDesc,
                      ForestClientDetailsDto::registryCompanyTypeCode,
                      ForestClientDetailsDto::corpRegnNmbr,
                      ForestClientDetailsDto::wcbFirmNumber,
                      ForestClientDetailsDto::clientComment,
                      ForestClientDetailsDto::goodStandingInd
                  )
                  .containsExactly(
                      expectedDto.clientNumber(),
                      expectedDto.clientName(),
                      expectedDto.clientStatusCode(),
                      expectedDto.clientStatusDesc(),
                      expectedDto.clientTypeCode(),
                      expectedDto.clientTypeDesc(),
                      expectedDto.registryCompanyTypeCode(),
                      expectedDto.corpRegnNmbr(),
                      expectedDto.wcbFirmNumber(),
                      expectedDto.clientComment(),
                      expectedDto.goodStandingInd()
                  );
          })
          .verifyComplete();
  }
  
  @Test
  @DisplayName("Retrieve active update reasons by client type and action code")
  void testFindActiveUpdateReasonsByClientTypeAndActionCode() {
      String clientTypeCode = "C";
      String actionCode = "NAME";

      CodeNameDto expectedDto = new CodeNameDto("CORR", "Correction");

      Logger logger = (Logger) LoggerFactory.getLogger(ClientLegacyService.class);

      ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
      listAppender.start();
      logger.addAppender(listAppender);
      
      legacyStub.stubFor(
          get(urlPathEqualTo("/api/codes/update-reasons/" + clientTypeCode + "/" + actionCode))
              .willReturn(okJson("[{\"code\":\"CORR\",\"name\":\"Correction\"}]"))
      );

      service
          .findActiveUpdateReasonsByClientTypeAndActionCode(clientTypeCode, actionCode)
          .as(StepVerifier::create)
          .assertNext(dto -> {
              assertEquals(expectedDto.code(), dto.code());
              assertEquals(expectedDto.name(), dto.name());
          })
          .verifyComplete();
      
      boolean logMessage1Found = listAppender.list.stream()
          .anyMatch(event -> event.getFormattedMessage().contains("Searching for client type") &&
                             event.getFormattedMessage().contains(clientTypeCode) &&
                             event.getFormattedMessage().contains(actionCode));

      boolean logMessage2Found = listAppender.list.stream()
          .anyMatch(event -> event.getFormattedMessage().contains("Found data for client type") &&
                             event.getFormattedMessage().contains(clientTypeCode) &&
                             event.getFormattedMessage().contains(actionCode));

      assertTrue(logMessage1Found, "Expected log message for searching not found.");
      assertTrue(logMessage2Found, "Expected log message for found data not found.");
  }
  
  @Test
  @DisplayName("Search clients by keyword with pagination")
  void shouldSearchClientsByKeyword() {
      int page = 1;
      int size = 10;
      String keyword = "John";

      Long expectedTotalCount = 25L;
      ClientListDto expectedDto = new ClientListDto(
          "00000001",
          "ACR",
          "John Doe",
          "Corporation",
          "Victoria",
          "Active"
      );

      legacyStub.stubFor(
          get(urlPathEqualTo("/api/search"))
              .withQueryParam("page", equalTo(String.valueOf(page)))
              .withQueryParam("size", equalTo(String.valueOf(size)))
              .withQueryParam("value", equalTo(keyword))
              .willReturn(
                  aResponse()
                      .withHeader("Content-Type", "application/json")
                      .withHeader("X-Total-Count", expectedTotalCount.toString())
                      .withBody("[{"
                          + "\"clientNumber\":\"00000001\","
                          + "\"clientAcronym\":\"ACR\","
                          + "\"clientFullName\":\"John Doe\","
                          + "\"clientType\":\"Corporation\","
                          + "\"city\":\"Victoria\","
                          + "\"clientStatus\":\"Active\""
                          + "}]")
              )
      );
      
      service
          .search(page, size, keyword)
          .as(StepVerifier::create)
          .assertNext(pair -> {
              ClientListDto actualDto = pair.getFirst();
              Long actualTotalCount = pair.getSecond();

              assertEquals(expectedDto.clientNumber(), actualDto.clientNumber());
              assertEquals(expectedDto.clientAcronym(), actualDto.clientAcronym());
              assertEquals(expectedDto.clientFullName(), actualDto.clientFullName());
              assertEquals(expectedDto.clientType(), actualDto.clientType());
              assertEquals(expectedDto.city(), actualDto.city());
              assertEquals(expectedDto.clientStatus(), actualDto.clientStatus());

              assertEquals(expectedTotalCount, actualTotalCount);
          })
          .verifyComplete();
  }

}