package ca.bc.gov.app.service.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.dto.client.CodeNameDto;
import ca.bc.gov.app.entity.client.CountryCodeEntity;
import ca.bc.gov.app.repository.client.CountryCodeRepository;
import java.time.LocalDate;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
@DisplayName("Integrated Test | FSA Client Service")
class ClientServiceIntegrationTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  private ClientService service;
  
  @Autowired
  private CountryCodeRepository countryCodeRepository;

  @ParameterizedTest
  @MethodSource("date")
  @DisplayName("Return code as long as the date allows it")
  void shouldListCodeAsExpected(LocalDate date) {
    StepVerifier
        .create(service.findActiveClientTypeCodes(date))
        .assertNext(results -> assertEquals("A", results.code()))
        .expectNextCount(11)
        .verifyComplete();
  }
  
  @Test
  void testGetCountryByCode() {

    CountryCodeEntity countryCodeEntity = new CountryCodeEntity("CA", "Canada");
    CodeNameDto expectedDto = new CodeNameDto("CA", "Canada");

    when(countryCodeRepository.findByCountryCode("CA")).thenReturn(Mono.just(countryCodeEntity));

    Mono<Object> resultMono = service.getCountryByCode("CA");

    StepVerifier.create(resultMono).expectNext(expectedDto).verifyComplete();
  }

  private static Stream<LocalDate> date() {
    return
        Stream.of(
            LocalDate.now(),
            LocalDate.now().plusDays(1)
        );
  }

}