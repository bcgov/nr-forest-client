package ca.bc.gov.app.service.client;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import ca.bc.gov.app.dto.legacy.AddressSearchDto;
import ca.bc.gov.app.dto.legacy.ContactSearchDto;
import ca.bc.gov.app.dto.legacy.ForestClientDetailsDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
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
  void shouldNotSearchWhenInvalidCasesHitGeneric(Map<String, List<String>> parameters){
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

}