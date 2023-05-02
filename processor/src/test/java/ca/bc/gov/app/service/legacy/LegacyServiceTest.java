package ca.bc.gov.app.service.legacy;

import ca.bc.gov.app.extensions.AbstractTestContainer;
import ca.bc.gov.app.service.client.ClientService;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

@Slf4j
@DisplayName("Integrated Test | Legacy Fuzzy Search")
class LegacyServiceTest extends AbstractTestContainer {
  @Autowired
  private LegacyService service;

  @Autowired
  private ClientService cservice;

  @ParameterizedTest(name = "{0} gives back {1}")
  @MethodSource("lookupFor")
  @DisplayName("Looking for")
  void shouldLookFor(String name, int returnSize) {

    service
        .matchBy(name)
        .as(StepVerifier::create)
        .expectNextCount(returnSize)
        .verifyComplete();

  }

  @Test
  void test1() {
    cservice
        .getSubmittedSubmissions()
        .as(StepVerifier::create)
        .expectNextCount(0)
        .verifyComplete();
  }

  private static Stream<Arguments> lookupFor() {
    return
        Stream.of(
            Arguments.of("bond", 2),
            Arguments.of("funny", 1),
            Arguments.of("boris", 1),
            Arguments.of("indian", 2),
            Arguments.of("forest", 2)
        );
  }

}