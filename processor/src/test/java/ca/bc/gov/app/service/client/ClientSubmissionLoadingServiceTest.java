package ca.bc.gov.app.service.client;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.entity.client.SubmissionTypeCodeEnum;
import ca.bc.gov.app.repository.client.SubmissionContactRepository;
import ca.bc.gov.app.repository.client.SubmissionDetailRepository;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Client Service")
class ClientSubmissionLoadingServiceTest {

  private final SubmissionDetailRepository submissionDetailRepository =
      mock(SubmissionDetailRepository.class);
  private final SubmissionContactRepository contactRepository =
      mock(SubmissionContactRepository.class);
  private final ClientSubmissionLoadingService service = new ClientSubmissionLoadingService(submissionDetailRepository,contactRepository);


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
                .containsEntry(ApplicationConstant.SUBMISSION_ID, 1)
        )
        .verifyComplete();

  }

  @Test
  @DisplayName("should send notification")
  void shouldSendNotification(){

    when(submissionDetailRepository.findBySubmissionId(any()))
        .thenReturn(Mono.just(TestConstants.SUBMISSION_DETAIL.withClientNumber("00001000")));
    when(contactRepository.findFirstBySubmissionId(any()))
        .thenReturn(Mono.just(TestConstants.SUBMISSION_CONTACT));



    service
        .sendNotification(
            MessageBuilder
                .withPayload(1)
                .setHeader(ApplicationConstant.SUBMISSION_ID, 1)
                .setHeader(ApplicationConstant.SUBMISSION_TYPE, SubmissionTypeCodeEnum.AAC)
                .setHeader(ApplicationConstant.FOREST_CLIENT_NAME, "Test")
                .setHeader(ApplicationConstant.FOREST_CLIENT_NUMBER, "00001000")
                .build()
        )
        .as(StepVerifier::create)
        .assertNext(message ->
            assertThat(message)
                .isNotNull()
                .isInstanceOf(Message.class)
                .hasFieldOrPropertyWithValue("payload", TestConstants.EMAIL_REQUEST_DTO)
                .hasFieldOrProperty("headers")
                .extracting(Message::getHeaders, as(InstanceOfAssertFactories.MAP))
                .isNotNull()
                .isNotEmpty()
                .containsKey("id")
                .containsKey("timestamp")
                .containsEntry(ApplicationConstant.SUBMISSION_ID, 1)
                .containsEntry(ApplicationConstant.SUBMISSION_TYPE, SubmissionTypeCodeEnum.AAC)
                .containsEntry(ApplicationConstant.FOREST_CLIENT_NAME, "Test")
                .containsEntry(ApplicationConstant.FOREST_CLIENT_NUMBER, "00001000")
        )
        .verifyComplete();
  }


}