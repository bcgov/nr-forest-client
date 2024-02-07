package ca.bc.gov.app.controller.client;

import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.dto.client.DistrictDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;

@DisplayName("Integrated Test | FSA Client District Controller")
class ClientDistrictControllerIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @BeforeEach
  public void reset() {
    client = client.mutate()
        .responseTimeout(Duration.ofSeconds(10))
        .build();
  }

  @Test
  @DisplayName("List districts")
  void shouldListDistricts() {

    client
        .get()
        .uri("/api/districts")
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
        .uri("/api/districts/{districtCode}", Map.of("districtCode", "DMH"))
        .exchange()
        .expectStatus().isOk()
        .expectBody(DistrictDto.class)
        .isEqualTo(new DistrictDto("DMH", "100 Mile House Natural Resource District","FLNR.100MileHouseDistrict@gov.bc.ca"));

  }



}
