package ca.bc.gov.app.service.client;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.ChannelConstant;
import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionMatchDetailRepository;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Client Service")
class ClientServiceTest {

  private final SubmissionDetailRepository submissionDetailRepository =
      mock(SubmissionDetailRepository.class);
  private final ClientService service =
      new ClientService(mock(SubmissionRepository.class), submissionDetailRepository, mock(
          SubmissionMatchDetailRepository.class));

  @Test
  @DisplayName("get submission as message")
  void shouldLoadSubmissionIntoMessage() {

    when(submissionDetailRepository.findBySubmissionId(1))
        .thenReturn(Mono.just(TestConstants.SUBMISSION_DETAIL));

    service
        .loadSubmissionDetails(1)
        .as(StepVerifier::create)
        .assertNext(message ->
            assertThat(message)
                .isNotNull()
                .isInstanceOf(Message.class)
                .hasFieldOrPropertyWithValue("payload", TestConstants.SUBMISSION_INFORMATION)
                .hasFieldOrProperty("headers")
                .extracting(Message::getHeaders, as(InstanceOfAssertFactories.MAP))
                .isNotNull()
                .isNotEmpty()
                .containsKey("id")
                .containsKey("timestamp")
                .containsEntry(ChannelConstant.SUBMISSION_ID, 1)
        )
        .verifyComplete();

  }
}
