package ca.bc.gov.app.service.client;

import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

@Slf4j
@DisplayName("Integrated Test | FSA Client Country and District Service")
class ClientCountryProvinceServiceIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  private ClientCountryProvinceService service;

  @Test
  void testGetCountryByCode() {

    CodeNameDto expectedDto = new CodeNameDto("CA", "Canada");

    service
        .getCountryByCode("CA")
        .as(StepVerifier::create)
        .expectNext(expectedDto)
        .verifyComplete();
  }


}