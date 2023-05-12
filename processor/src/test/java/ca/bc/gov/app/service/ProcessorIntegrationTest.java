package ca.bc.gov.app.service;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ca.bc.gov.app.entity.client.SubmissionEntity;
import ca.bc.gov.app.extensions.AbstractTestContainer;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import ca.bc.gov.app.repository.legacy.ForestClientRepository;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.integration.support.MessageBuilder;

@DisplayName("Integration Test | Processor End-to-End Flow")
class ProcessorIntegrationTest extends AbstractTestContainer {

  @SpyBean
  private SubmissionRepository submissionRepository;

  @SpyBean
  private SubmissionDetailRepository detailRepository;

  @SpyBean
  private ForestClientRepository forestClientRepository;

  @Autowired
  private FluxMessageChannel submissionListChannel;

  @Test
  @DisplayName("Process one entity")
  void test() {
    submissionListChannel.send(MessageBuilder.withPayload(1).build());

    await()
        .alias("Load details")
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() -> verify(detailRepository, times(1)).findBySubmissionId(eq(1)));

    await()
        .alias("Fuzzy Search")
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() -> verify(forestClientRepository, times(2)).matchBy(any(String.class)));

    await()
        .alias("Incorporation Search")
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() -> verify(forestClientRepository, times(2)).findByIncorporationNumber(any(String.class)));

    await()
        .alias("Submission lookup")
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() ->
            verify(submissionRepository, times(2)).findById(eq(1))
        );

    await()
        .alias("Submission persistence")
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() ->
            verify(submissionRepository, times(2)).save(any(SubmissionEntity.class))
        );


  }
}
