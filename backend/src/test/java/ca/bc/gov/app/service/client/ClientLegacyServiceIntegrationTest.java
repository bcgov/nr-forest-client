package ca.bc.gov.app.service.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.client.ClientListDto;
import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.legacy.AddressSearchDto;
import ca.bc.gov.app.dto.legacy.ContactSearchDto;
import ca.bc.gov.app.dto.legacy.ForestClientDetailsDto;
import ca.bc.gov.app.dto.legacy.ForestClientInformationDto;
import ca.bc.gov.app.dto.legacy.HistoryLogDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.springframework.beans.factory.annotation.Autowired;
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
  void setUp() {
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
          new ForestClientInformationDto(
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
              null
          ),
          null,
          null,
          null,
          Map.of()
      );

      legacyStub
          .stubFor(
              get(urlPathEqualTo("/api/search/clientNumber/" + clientNumber))
                  .willReturn(okJson("""
                      {
                        "client":{
                          "clientNumber":"00000001",
                          "clientName":"MY COMPANY LTD.",
                          "legalFirstName":null,
                          "legalMiddleName":null,
                          "clientStatusCode":"ACT",
                          "clientStatusDesc":"Active",
                          "clientTypeCode":"C",
                          "clientTypeDesc":"Corporation",
                          "clientIdTypeCode":null,
                          "clientIdTypeDesc":null,
                          "clientIdentification":null,
                          "registryCompanyTypeCode":"BC",
                          "corpRegnNmbr":"9607514",
                          "clientAcronym":null,
                          "wcbFirmNumber":"678",
                          "ocgSupplierNmbr":null,
                          "clientComment":"THIS TEST",
                          "clientCommentUpdateDate":null,
                          "clientCommentUpdateUser":null,
                          "goodStandingInd":"Y",
                          "birthdate":null
                        },
                        "addresses":null,
                        "contacts":null,
                        "doingBusinessAs":null
                      }"""))
                  .withHeader("Content-Type", equalTo("application/json"))
          );

      service.searchByClientNumber(clientNumber)
          .as(StepVerifier::create)
          .assertNext(clientDetailsDto -> {
              assertThat(clientDetailsDto)
                  .extracting(
                      forestClientDetailsDto -> forestClientDetailsDto.client().clientNumber(),
                      forestClientDetailsDto -> forestClientDetailsDto.client().clientName(),
                      forestClientDetailsDto -> forestClientDetailsDto.client().clientStatusCode(),
                      forestClientDetailsDto -> forestClientDetailsDto.client().clientStatusDesc(),
                      forestClientDetailsDto -> forestClientDetailsDto.client().clientTypeCode(),
                      forestClientDetailsDto -> forestClientDetailsDto.client().clientTypeDesc(),
                      forestClientDetailsDto -> forestClientDetailsDto.client().registryCompanyTypeCode(),
                      forestClientDetailsDto -> forestClientDetailsDto.client().corpRegnNmbr(),
                      forestClientDetailsDto -> forestClientDetailsDto.client().wcbFirmNumber(),
                      forestClientDetailsDto -> forestClientDetailsDto.client().clientComment(),
                      forestClientDetailsDto -> forestClientDetailsDto.client().goodStandingInd()
                  )
                  .containsExactly(
                      expectedDto.client().clientNumber(),
                      expectedDto.client().clientName(),
                      expectedDto.client().clientStatusCode(),
                      expectedDto.client().clientStatusDesc(),
                      expectedDto.client().clientTypeCode(),
                      expectedDto.client().clientTypeDesc(),
                      expectedDto.client().registryCompanyTypeCode(),
                      expectedDto.client().corpRegnNmbr(),
                      expectedDto.client().wcbFirmNumber(),
                      expectedDto.client().clientComment(),
                      expectedDto.client().goodStandingInd()
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
  
  @Test
  @DisplayName("Retrieve active client statuses")
  void testFindActiveClientStatusCodes() {
      CodeNameDto expectedActiveDto = new CodeNameDto("ACT", "Active");
      CodeNameDto expectedDeactivatedDto = new CodeNameDto("DAC", "Deactivated");
      CodeNameDto expectedDeceasedDto = new CodeNameDto("DEC", "Deceased");
      CodeNameDto expectedReceivershipDto = new CodeNameDto("REC", "Receivership");
      CodeNameDto expectedSuspendedDto = new CodeNameDto("SPN", "Suspended");

      legacyStub.stubFor(
          get(urlPathEqualTo("/api/codes/client-statuses"))
              .willReturn(okJson("["
                  + "{\"code\":\"ACT\",\"name\":\"Active\"},"
                  + "{\"code\":\"DAC\",\"name\":\"Deactivated\"},"
                  + "{\"code\":\"DEC\",\"name\":\"Deceased\"},"
                  + "{\"code\":\"REC\",\"name\":\"Receivership\"},"
                  + "{\"code\":\"SPN\",\"name\":\"Suspended\"}"
                  + "]"))
      );

      service
          .findActiveClientStatusCodes()
          .as(StepVerifier::create)
          .assertNext(dto -> {
              assertEquals(expectedActiveDto.code(), dto.code());
              assertEquals(expectedActiveDto.name(), dto.name());
          })
          .assertNext(dto -> {
              assertEquals(expectedDeactivatedDto.code(), dto.code());
              assertEquals(expectedDeactivatedDto.name(), dto.name());
          })
          .assertNext(dto -> {
              assertEquals(expectedDeceasedDto.code(), dto.code());
              assertEquals(expectedDeceasedDto.name(), dto.name());
          })
          .assertNext(dto -> {
              assertEquals(expectedReceivershipDto.code(), dto.code());
              assertEquals(expectedReceivershipDto.name(), dto.name());
          })
          .assertNext(dto -> {
              assertEquals(expectedSuspendedDto.code(), dto.code());
              assertEquals(expectedSuspendedDto.name(), dto.name());
          })
          .verifyComplete();
  }

  @Test
  @DisplayName("Test active client statuses for admin role")
  void testFindActiveClientStatusForAdmin() {
      String clientTypeCode = "F";
      Set<String> groups = Set.of(ApplicationConstant.ROLE_ADMIN);

      CodeNameDto expectedActiveDto = new CodeNameDto("ACT", "Active");
      CodeNameDto expectedDeactivatedDto = new CodeNameDto("DAC", "Deactivated");

      legacyStub.stubFor(
          get(urlPathEqualTo("/api/codes/client-statuses"))
              .willReturn(okJson("["
                  + "{\"code\":\"ACT\",\"name\":\"Active\"},"
                  + "{\"code\":\"DAC\",\"name\":\"Deactivated\"}"
                  + "]"))
      );

      service
          .findActiveClientStatusCodesByClientTypeAndRole(clientTypeCode, groups)
          .as(StepVerifier::create)
          .assertNext(dto -> {
              assertEquals(expectedActiveDto.code(), dto.code());
              assertEquals(expectedActiveDto.name(), dto.name());
          })
          .assertNext(dto -> {
              assertEquals(expectedDeactivatedDto.code(), dto.code());
              assertEquals(expectedDeactivatedDto.name(), dto.name());
          })
          .verifyComplete();
  }

  @Test
  @DisplayName("Test active client statuses for editor role")
  void testFindActiveClientStatusForEditor() {
      String individualClientTypeCode = "I";
      Set<String> groups = Set.of(ApplicationConstant.ROLE_EDITOR);

      CodeNameDto expectedActiveDto = new CodeNameDto("ACT", "Active");
      CodeNameDto expectedDeactivedDto = new CodeNameDto("DAC", "Deactivated");

      legacyStub.stubFor(
          get(urlPathEqualTo("/api/codes/client-statuses"))
              .willReturn(okJson("["
                  + "{\"code\":\"ACT\",\"name\":\"Active\"},"
                  + "{\"code\":\"DAC\",\"name\":\"Deactivated\"}"
                  + "]"))
      );

      service
          .findActiveClientStatusCodesByClientTypeAndRole(individualClientTypeCode, groups)
          .as(StepVerifier::create)
          .assertNext(dto -> {
              assertEquals(expectedActiveDto.code(), dto.code());
              assertEquals(expectedActiveDto.name(), dto.name());
          })
          .assertNext(dto -> {
              assertEquals(expectedDeactivedDto.code(), dto.code());
              assertEquals(expectedDeactivedDto.name(), dto.name());
          })
          .verifyComplete();
  }
  
  @Test
  @DisplayName("Test active client statuses for suspend role")
  void testFindActiveClientStatusForSuspend() {
      String individualClientTypeCode = "I";
      Set<String> groups = Set.of(ApplicationConstant.ROLE_SUSPEND);

      CodeNameDto expectedActiveDto = new CodeNameDto("ACT", "Active");
      CodeNameDto expectedSuspendedDto = new CodeNameDto("SPN", "Suspended");
      CodeNameDto expectedReceivershipDto = new CodeNameDto("REC", "Receivership");

      legacyStub.stubFor(
          get(urlPathEqualTo("/api/codes/client-statuses"))
              .willReturn(okJson("["
                  + "{\"code\":\"ACT\",\"name\":\"Active\"},"
                  + "{\"code\":\"SPN\",\"name\":\"Suspended\"},"
                  + "{\"code\":\"REC\",\"name\":\"Receivership\"}"
                  + "]"))
      );

      service
          .findActiveClientStatusCodesByClientTypeAndRole(individualClientTypeCode, groups)
          .as(StepVerifier::create)
          .assertNext(dto -> {
              assertEquals(expectedActiveDto.code(), dto.code());
              assertEquals(expectedActiveDto.name(), dto.name());
          })
          .assertNext(dto -> {
              assertEquals(expectedSuspendedDto.code(), dto.code());
              assertEquals(expectedSuspendedDto.name(), dto.name());
          })
          .assertNext(dto -> {
            assertEquals(expectedReceivershipDto.code(), dto.code());
            assertEquals(expectedReceivershipDto.name(), dto.name());
          })
          .verifyComplete();
  }
  
  @Test
  @DisplayName("Test no client status is returned when role is not admin or editor")
  void testFindActiveClientStatusCodesByClientTypeAndRole_NoValidRole() {
      String clientTypeCode = "B";
      Set<String> groups = Set.of("SOME_OTHER_ROLE");

      legacyStub.stubFor(
          get(urlPathEqualTo("/api/codes/client-statuses"))
              .willReturn(okJson("["
                  + "{\"code\":\"ACT\",\"name\":\"Active\"},"
                  + "{\"code\":\"DAC\",\"name\":\"Deactivated\"},"
                  + "{\"code\":\"DEC\",\"name\":\"Deceased\"},"
                  + "{\"code\":\"REC\",\"name\":\"Receivership\"},"
                  + "{\"code\":\"SPN\",\"name\":\"Suspended\"}"
                  + "]"))
      );

      service
          .findActiveClientStatusCodesByClientTypeAndRole(clientTypeCode, groups)
          .as(StepVerifier::create)
          .verifyComplete();
  }
  
  @Test
  @DisplayName("Test admin default statuses are returned for unlisted clientTypeCode")
  void testFindActiveClientStatusCodesByClientTypeAndRole_AdminDefaultCase() {
      String clientTypeCode = "X";
      Set<String> groups = Set.of(ApplicationConstant.ROLE_ADMIN);

      legacyStub.stubFor(
          get(urlPathEqualTo("/api/codes/client-statuses"))
              .willReturn(okJson("["
                  + "{\"code\":\"ACT\",\"name\":\"Active\"},"
                  + "{\"code\":\"DAC\",\"name\":\"Deactivated\"},"
                  + "{\"code\":\"DEC\",\"name\":\"Deceased\"},"
                  + "{\"code\":\"REC\",\"name\":\"Receivership\"},"
                  + "{\"code\":\"SPN\",\"name\":\"Suspended\"}"
                  + "]"))
      );

      service
          .findActiveClientStatusCodesByClientTypeAndRole(clientTypeCode, groups)
          .as(StepVerifier::create)
          .assertNext(dto -> assertEquals("ACT", dto.code()))
          .assertNext(dto -> assertEquals("DAC", dto.code()))
          .assertNext(dto -> assertEquals("REC", dto.code()))
          .assertNext(dto -> assertEquals("SPN", dto.code()))
          .verifyComplete();
  }
  
  @Test
  @DisplayName("Retrieve active registry types")
  void testFindActiveRegistryTypeCodes() {
      CodeNameDto expecteDto = new CodeNameDto("FM", "Sole Proprietorship");

      legacyStub.stubFor(
          get(urlPathEqualTo("/api/codes/registry-types"))
              .willReturn(okJson("[{\"code\":\"FM\",\"name\":\"Sole Proprietorship\"}]"))
      );

      service
          .findActiveRegistryTypeCodes()
          .as(StepVerifier::create)
          .assertNext(dto -> {
              assertEquals(expecteDto.code(), dto.code());
              assertEquals(expecteDto.name(), dto.name());
          })
          .verifyComplete();
  }
  
  @Test
  @DisplayName("Should retrieve history logs from legacy API by client number")
  void shouldRetrieveHistoryLogsFromLegacyApi() {
      String clientNumber = "00000138";
      int page = 0;
      int size = 5;
      List<String> sources = List.of("CLI");
      Long expectedTotalCount = 1L;

      legacyStub.stubFor(
          get(urlPathEqualTo("/api/clients/history-logs/" + clientNumber))
              .withQueryParam("page", equalTo(String.valueOf(page)))
              .withQueryParam("size", equalTo(String.valueOf(size)))
              .withQueryParam("sources", equalTo("CLI"))
              .willReturn(
                  aResponse()
                      .withHeader("Content-Type", "application/json")
                      .withHeader("X-Total-Count", expectedTotalCount.toString())
                      .withBody("[{" 
                          + "\"tableName\":\"ClientInformation\","
                          + "\"idx\":\"123\","
                          + "\"identifierLabel\":\"Client summary updated\","
                          + "\"updateTimestamp\":\"2007-09-14T10:15:41\","
                          + "\"updateUserid\":\"test_user\","
                          + "\"changeType\":\"UPD\","
                          + "\"details\":"
                          + "[{\"columnName\":\"clientName\"," 
                          + "  \"oldValue\":\"Jhon Doe\","
                          + "  \"newValue\":\"John Doe\"}"
                          + "],"
                          + "\"reasons\":"
                          + "[{\"actionCode\":\"NAME\","
                          + "  \"reason\":\"Correction\"}"
                          + "]"
                          + "}]"
                      )
              )
      );

      service
          .retrieveHistoryLogs(clientNumber, page, size, sources)
          .as(StepVerifier::create)
          .expectNextMatches(pair -> {
              HistoryLogDto dto = pair.getFirst();
              return dto.tableName().equals("ClientInformation")
                  && dto.details().get(0).columnName().equals("clientName");
          })
          .verifyComplete();
  }
  
  @Test
  @DisplayName("Retrieve active registry types by client type")
  void testFindActiveRegistryTypesByClientType() {
      String clientTypeCode = "C";

      CodeNameDto expectedDto = new CodeNameDto("BC", "British Columbia Company");

      Logger logger = (Logger) LoggerFactory.getLogger(ClientLegacyService.class);

      ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
      listAppender.start();
      logger.addAppender(listAppender);
      
      legacyStub.stubFor(
          get(urlPathEqualTo("/api/codes/registry-types/" + clientTypeCode))
              .willReturn(okJson("[{\"code\":\"BC\",\"name\":\"British Columbia Company\"}]"))
      );

      service
          .findActiveRegistryTypeCodesByClientTypeCode(clientTypeCode)
          .as(StepVerifier::create)
          .expectNext(expectedDto)
          .verifyComplete();
  }
  
  @Test
  @DisplayName("Retrieve active client types")
  void testFindActiveClientTypes() {

      CodeNameDto expectedDto = new CodeNameDto("C", "Corporation");

      Logger logger = (Logger) LoggerFactory.getLogger(ClientLegacyService.class);

      ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
      listAppender.start();
      logger.addAppender(listAppender);
      
      legacyStub.stubFor(
          get(urlPathEqualTo("/api/codes/client-types"))
              .willReturn(aResponse()
                  .withStatus(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody("[{\"code\":\"C\",\"name\":\"Corporation\"}]"))
      );

      service
          .findActiveClientTypeCodes()
          .as(StepVerifier::create)
          .expectNext(expectedDto)
          .verifyComplete();
  }
  
  @Test
  @DisplayName("Retrieve active client ID types")
  void testFindActiveClientIdTypes() {

      CodeNameDto expectedDto = new CodeNameDto("BCDL", "British Columbia Drivers Licence");

      Logger logger = (Logger) LoggerFactory.getLogger(ClientLegacyService.class);

      ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
      listAppender.start();
      logger.addAppender(listAppender);
      
      legacyStub.stubFor(
          get(urlPathEqualTo("/api/codes/client-id-types"))
              .willReturn(okJson("[{\"code\":\"BCDL\",\"name\":\"British Columbia Drivers Licence\"}]"))
      );

      service
          .findActiveIdentificationTypeCodes()
          .as(StepVerifier::create)
          .expectNext(expectedDto)
          .verifyComplete();
  }
  
  @Test
  @DisplayName("Retrieve active relationship types by client type")
  void testFindActiveRelationshipTypesByClientType() {
      String clientTypeCode = "C";

      CodeNameDto expectedDto = new CodeNameDto("AG", "Agent");

      Logger logger = (Logger) LoggerFactory.getLogger(ClientLegacyService.class);

      ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
      listAppender.start();
      logger.addAppender(listAppender);
      
      legacyStub.stubFor(
          get(urlPathEqualTo("/api/codes/relationship-types/" + clientTypeCode))
              .willReturn(okJson("[{\"code\":\"AG\",\"name\":\"Agent\"}]"))
      );

      service
          .findActiveRelationshipCodesByClientTypeCode(clientTypeCode)
          .as(StepVerifier::create)
          .expectNext(expectedDto)
          .verifyComplete();
  }

  @Test
  @DisplayName("Retrieve all locations updated with client by status code")
  void testFindAllLocationUpdatedWithClient() {
      String clientNumber = "00000001";
      String statusCode = "DAC";

      CodeNameDto expectedLocation1 = new CodeNameDto("00", "Main Office");
      CodeNameDto expectedLocation2 = new CodeNameDto("01", "Branch Office");

      Logger logger = (Logger) LoggerFactory.getLogger(ClientLegacyService.class);

      ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
      listAppender.start();
      logger.addAppender(listAppender);

      legacyStub.stubFor(
          get(urlPathEqualTo("/api/locations/" + clientNumber + "/" + statusCode))
              .willReturn(okJson("["
                  + "{\"code\":\"00\",\"name\":\"Main Office\"},"
                  + "{\"code\":\"01\",\"name\":\"Branch Office\"}"
                  + "]"))
      );

      service
          .findAllLocationUpdatedWithClient(clientNumber, statusCode)
          .as(StepVerifier::create)
          .assertNext(dto -> {
              assertEquals(expectedLocation1.code(), dto.code());
              assertEquals(expectedLocation1.name(), dto.name());
          })
          .assertNext(dto -> {
              assertEquals(expectedLocation2.code(), dto.code());
              assertEquals(expectedLocation2.name(), dto.name());
          })
          .verifyComplete();

      boolean searchLogFound = listAppender.list.stream()
          .anyMatch(event -> event.getFormattedMessage().contains("Searching for all location updated with client") &&
                             event.getFormattedMessage().contains(clientNumber) &&
                             event.getFormattedMessage().contains(statusCode));

      boolean foundLogFound = listAppender.list.stream()
          .anyMatch(event -> event.getFormattedMessage().contains("Found location") &&
                             event.getFormattedMessage().contains("updated with client in legacy with status code") &&
                             event.getFormattedMessage().contains(statusCode));

      assertTrue(searchLogFound, "Expected log message for searching not found.");
      assertTrue(foundLogFound, "Expected log message for found location not found.");
  }

  @Test
  @DisplayName("Retrieve empty list when no locations updated with client")
  void testFindAllLocationUpdatedWithClientReturnsEmpty() {
      String clientNumber = "00000001";
      String statusCode = "DAC";

      legacyStub.stubFor(
          get(urlPathEqualTo("/api/locations/" + clientNumber + "/" + statusCode))
              .willReturn(okJson("[]"))
      );

      service
          .findAllLocationUpdatedWithClient(clientNumber, statusCode)
          .as(StepVerifier::create)
          .verifyComplete();
  }

}