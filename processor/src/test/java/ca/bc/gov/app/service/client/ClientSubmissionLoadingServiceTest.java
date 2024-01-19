package ca.bc.gov.app.service.client;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.TestConstants;
import ca.bc.gov.app.dto.MessagingWrapper;
import ca.bc.gov.app.repository.SubmissionContactRepository;
import ca.bc.gov.app.repository.SubmissionDetailRepository;
import ca.bc.gov.app.repository.SubmissionLocationRepository;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Unit Test | Client Service")
class ClientSubmissionLoadingServiceTest {

  private final SubmissionDetailRepository submissionDetailRepository =
      mock(SubmissionDetailRepository.class);
  private final SubmissionContactRepository contactRepository =
      mock(SubmissionContactRepository.class);
  private final SubmissionLocationRepository locationRepository =
	      mock(SubmissionLocationRepository.class);
  private final ClientSubmissionLoadingService service = new ClientSubmissionLoadingService(
      submissionDetailRepository, contactRepository, locationRepository);


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
                .isInstanceOf(MessagingWrapper.class)
                .hasFieldOrPropertyWithValue("payload", TestConstants.SUBMISSION_INFORMATION)
                .hasFieldOrProperty("parameters")
                .extracting(MessagingWrapper::parameters, as(InstanceOfAssertFactories.MAP))
                .isNotNull()
                .isNotEmpty()
                .containsKey("submission-id")
                .containsEntry(ApplicationConstant.SUBMISSION_ID, 1)
        )
        .verifyComplete();

  }


}
