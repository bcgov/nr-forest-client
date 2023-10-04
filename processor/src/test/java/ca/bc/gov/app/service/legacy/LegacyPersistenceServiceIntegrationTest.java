package ca.bc.gov.app.service.legacy;


import static org.assertj.core.api.Assertions.assertThat;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.extensions.AbstractTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import reactor.test.StepVerifier;

@DisplayName("Integration Test | Legacy Persistence Service")
class LegacyPersistenceServiceIntegrationTest extends AbstractTestContainer {

  @Autowired
  private LegacyPersistenceService service;

  @Test
  @DisplayName("load submission with id 2")
  void shouldLoadSubmissions(){

    service
        .loadSubmission(MessageBuilder
            .withPayload(2)
            .build()
        )
        .as(StepVerifier::create)
        .assertNext(message -> {
              assertThat(message)
                  .isNotNull()
                  .hasFieldOrPropertyWithValue("payload", 2);

              assertThat(message.getHeaders().get(ApplicationConstant.SUBMISSION_ID))
                  .isNotNull()
                  .isInstanceOf(Integer.class)
                  .isEqualTo(2);

          assertThat(message.getHeaders().get(ApplicationConstant.CREATED_BY))
              .isNotNull()
              .isInstanceOf(String.class);

          assertThat(message.getHeaders().get(ApplicationConstant.UPDATED_BY))
              .isNotNull()
              .isInstanceOf(String.class);

        })
        .verifyComplete();

  }

  @Test
  void shouldCreateForestClient(){
    service
        .createForestClient(
            MessageBuilder
            .withPayload(2)
            .build()
        )
        .as(StepVerifier::create)
        .assertNext(message -> {
          assertThat(message)
              .isNotNull()
              .hasFieldOrPropertyWithValue("payload", 2);

          assertThat(message.getHeaders().get(ApplicationConstant.SUBMISSION_ID))
              .isNotNull()
              .isInstanceOf(Integer.class)
              .isEqualTo(2);

          assertThat(message.getHeaders().get(ApplicationConstant.CREATED_BY))
              .isNotNull()
              .isInstanceOf(String.class);

          assertThat(message.getHeaders().get(ApplicationConstant.UPDATED_BY))
              .isNotNull()
              .isInstanceOf(String.class);

          assertThat(message.getHeaders().get(ApplicationConstant.FOREST_CLIENT_NAME))
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("Billy");

          assertThat(message.getHeaders().get(ApplicationConstant.INCORPORATION_NUMBER))
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("123456789");

          assertThat(message.getHeaders().get(ApplicationConstant.FOREST_CLIENT_NUMBER))
              .isNotNull()
              .isInstanceOf(String.class)
              .isEqualTo("123456789");

        })
        .verifyComplete();
  }

}