package ca.bc.gov.app.controller.client;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.client.DistrictDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;

@DisplayName("Integrated Test | FSA Client Codes Controller")
class ClientCodesControllerIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @BeforeEach
  public void reset() {
    client = client
        .mutateWith(csrf())
        .mutateWith(mockUser().roles(ApplicationConstant.USERTYPE_BCSC_USER))
        .mutate()
        .responseTimeout(Duration.ofSeconds(10))
        .build();
  }

  @Test
  @DisplayName("Codes are in expected order")
  void shouldListCodesAsExpected() {

    client
        .get()
        .uri("/api/codes/client-types")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].code").isNotEmpty()
        .jsonPath("$[0].code").isEqualTo("A")

        .jsonPath("$[1].code").isNotEmpty()
        .jsonPath("$[1].code").isEqualTo("C");

  }

  @ParameterizedTest(name = "{2} - {3} is the first on page {0} with size {1}")
  @MethodSource("contactTypeCodes")
  @DisplayName("List contact type codes")
  void shouldListContactTypes(Integer page, Integer size, String code, String description) {
    Function<UriBuilder, URI> uri = uriBuilder -> {

      UriBuilder localBuilder = uriBuilder
          .path("/api/codes/contact-types");

      if (page != null) {
        localBuilder = localBuilder.queryParam("page", page);
      }
      if (size != null) {
        localBuilder = localBuilder.queryParam("size", size);
      }

      return localBuilder.build(new HashMap<>());
    };

    client
        .get()
        .uri(uri)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].code").isNotEmpty()
        .jsonPath("$[0].code").isEqualTo(code)
        .jsonPath("$[0].name").isNotEmpty()
        .jsonPath("$[0].name").isEqualTo(description);
  }


  private static Stream<Arguments> contactTypeCodes() {
    return
        Stream.of(
            Arguments.of(null, null, "TC", "BCTS Contractor"),
            Arguments.of(0, 1, "TC", "BCTS Contractor")
        );
  }
  @ParameterizedTest(name = "{2} - {3} is the first on page {0} with size {1}")
  @MethodSource("countryCode")
  @DisplayName("List countries by")
  void shouldListCountryData(Integer page, Integer size, String code, String name) {

    //This is to allow parameter to be ommitted during test
    Function<UriBuilder, URI> uri = uriBuilder -> {

      UriBuilder localBuilder = uriBuilder
          .path("/api/codes/countries");

      if (page != null) {
        localBuilder = localBuilder.queryParam("page", page);
      }
      if (size != null) {
        localBuilder = localBuilder.queryParam("size", size);
      }

      return localBuilder.build(new HashMap<>());
    };

    client
        .get()
        .uri(uri)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].code").isNotEmpty()
        .jsonPath("$[0].code").isEqualTo(code)
        .jsonPath("$[0].name").isNotEmpty()
        .jsonPath("$[0].name").isEqualTo(name);
  }
  
  @ParameterizedTest(name = "{3} - {4} is the first on page {1} with size {2} for country {0}")
  @MethodSource("provinceCode")
  @DisplayName("List provinces by")
  void shouldListProvinceData(String countryCode, Integer page, Integer size, String code,
      String name) {

    //This is to allow parameter to be ommitted during test
    Function<UriBuilder, URI> uri = uriBuilder -> {

      UriBuilder localBuilder = uriBuilder
          .path("/api/codes/countries/{countryCode}/provinces");

      if (page != null) {
        localBuilder = localBuilder.queryParam("page", page);
      }
      if (size != null) {
        localBuilder = localBuilder.queryParam("size", size);
      }

      return localBuilder.build(Map.of("countryCode", countryCode));
    };

    client
        .get()
        .uri(uri)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].code").isNotEmpty()
        .jsonPath("$[0].code").isEqualTo(code)
        .jsonPath("$[0].name").isNotEmpty()
        .jsonPath("$[0].name").isEqualTo(name);
  }

  @Test
  @DisplayName("get country by code")
  void shouldGetCountryByCode() {

    client
        .get()
        .uri("/api/codes/countries/{countryCode}", Map.of("countryCode", "CA"))
        .exchange()
        .expectStatus().isOk()
        .expectBody(CodeNameDto.class)
        .isEqualTo(new CodeNameDto("CA", "Canada"));

  }

  private static Stream<Arguments> countryCode() {
    return
        Stream.of(
            Arguments.of(null, null, "CA", "Canada"),
            Arguments.of(0, 1, "CA", "Canada"),
            Arguments.of(1, 1, "US", "United States of America"),
            Arguments.of(7, null, "EE", "Estonia"),
            Arguments.of(3, 10, "BA", "Bosnia and Herzegovina"),
            Arguments.of(33, 1, "BR", "Brazil"),
            Arguments.of(49, 1, "CO", "Colombia")
        );
  }

  private static Stream<Arguments> provinceCode() {
    return
        Stream.of(
            Arguments.of("CA", null, null, "AB", "Alberta"),
            Arguments.of("CA", 0, 1, "AB", "Alberta"),
            Arguments.of("US", 1, 1, "AK", "Alaska"));
  }
  
  @Test
  @DisplayName("List districts")
  void shouldListDistricts() {

    client
        .get()
        .uri("/api/codes/districts")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].code").isNotEmpty()
        .jsonPath("$[0].code").isEqualTo("DMH")
        .jsonPath("$[0].name").isNotEmpty()
        .jsonPath("$[0].name").isEqualTo("100 Mile House Natural Resource District");
  }

  @Test
  @DisplayName("get district by code")
  void shouldGetDistrictByCode() {

    client
        .get()
        .uri("/api/codes/districts/{districtCode}", Map.of("districtCode", "DMH"))
        .exchange()
        .expectStatus().isOk()
        .expectBody(DistrictDto.class)
        .isEqualTo(new DistrictDto("DMH", "100 Mile House Natural Resource District","FLNR.100MileHouseDistrict@gov.bc.ca"));

  }
  
}
