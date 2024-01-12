package ca.bc.gov.app.service.legacy;


import static org.assertj.core.api.Assertions.assertThat;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.extensions.AbstractTestContainer;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

@DisplayName("Integration Test | Legacy Persistence Service")
class LegacyAbstractPersistenceServiceIntegrationTest extends AbstractTestContainer {

  @Autowired
  private LegacyClientPersistenceService service;

  @Test
  @DisplayName("load submission with id 2")
  void shouldLoadSubmissions() {

    service
        .loadSubmission(new MessagingWrapper<>(2, Map.of()))
        .as(StepVerifier::create)
        .assertNext(message -> {
          assertThat(message)
              .isNotNull()
              .hasFieldOrPropertyWithValue("payload", 2);

          assertThat(message.parameters().get(ApplicationConstant.SUBMISSION_ID))
              .isNotNull()
              .isInstanceOf(Integer.class)
              .isEqualTo(2);

          assertThat(message.parameters().get(ApplicationConstant.CREATED_BY))
              .isNotNull()
              .isInstanceOf(String.class);

          assertThat(message.parameters().get(ApplicationConstant.UPDATED_BY))
              .isNotNull()
              .isInstanceOf(String.class);

        })
        .verifyComplete();

  }

}
