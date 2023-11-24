package ca.bc.gov.app.service.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.entity.client.ClientTypeCodeEntity;
import ca.bc.gov.app.entity.client.CountryCodeEntity;
import ca.bc.gov.app.repository.client.ClientTypeCodeRepository;
import ca.bc.gov.app.repository.client.CountryCodeRepository;
import java.time.LocalDate;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
@DisplayName("Integrated Test | FSA Client Service")
class ClientServiceIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  private ClientService service;
  
  @Mock
  private CountryCodeRepository countryCodeRepository;
  
  @Mock
  private ClientTypeCodeRepository clientTypeCodeRepository;

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
  void testGetCountryByCode() {

    CountryCodeEntity countryCodeEntity = new CountryCodeEntity("CA", "Canada");
    CodeNameDto expectedDto = new CodeNameDto("CA", "Canada");

    when(countryCodeRepository.findByCountryCode("CA")).thenReturn(Mono.just(countryCodeEntity));

    service
        .getCountryByCode("CA")
            .as(StepVerifier::create)
        .expectNext(expectedDto)
        .verifyComplete();
  }
  
  @Test
  void testGetClientTypeByCode() {

    ClientTypeCodeEntity clientTypeCodeEntity = new ClientTypeCodeEntity("CA", "Canada");
    CodeNameDto expectedDto = new CodeNameDto("RSP", "Registered sole proprietorship");

    when(clientTypeCodeRepository
        .findByCode("RSP"))
        .thenReturn(Mono.just(clientTypeCodeEntity));

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