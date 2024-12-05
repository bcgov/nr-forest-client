package ca.bc.gov.app.service.client;

import ca.bc.gov.app.dto.client.DistrictDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

@Slf4j
@DisplayName("Integrated Test | FSA Client District Service")
class ClientDistrictServiceIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  private ClientDistrictService service;

  @Test
  @DisplayName("Return district by code")
  void testGetDistrictByCode() {

    DistrictDto expectedDto =
        new DistrictDto("DMH", "100 Mile House Natural Resource District",
            "mail@mail.ca");

    service
        .getDistrictByCode("DMH")
        .as(StepVerifier::create)
        .expectNext(expectedDto)
        .verifyComplete();
  }

}
