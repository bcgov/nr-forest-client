package ca.bc.gov.app.service.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import java.time.LocalDate;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

@Slf4j
@DisplayName("Integrated Test | FSA Client Code Service")
public class ClientCodeServiceIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  private ClientCodeService service;

  @ParameterizedTest
  @MethodSource("date")
  @DisplayName("Return code as long as the date allows it")
  void shouldListCodeAsExpected(LocalDate date) {
    StepVerifier
        .create(service.findActiveClientTypeCodes(date))
        .assertNext(results -> assertEquals("A", results.code()))
        .expectNextCount(13)
        .verifyComplete();
  }

  @Test
  void testGetClientTypeByCode() {

    CodeNameDto expectedDto = new CodeNameDto("RSP", "Registered sole proprietorship");

    service
        .getClientTypeByCode("RSP")
        .as(StepVerifier::create)
        .expectNext(expectedDto)
        .verifyComplete();
  }

  private static Stream<LocalDate> date() {
    return
        Stream.of(
            LocalDate.now(),
            LocalDate.now().plusDays(1)
        );
  }
}
