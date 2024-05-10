package ca.bc.gov.app.service.client;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

@Slf4j
@DisplayName("Integrated Test | FSA Client Submission Service")
class ClientSubmissionServiceTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  private ClientSubmissionService service;

  @Test
  @DisplayName("Should return SubmissionDetailsDto when submission id is valid")
  void shouldReturnDetailsWithoutDuplicatedMatches() {

    service
        .getSubmissionDetail(365L)
        .as(StepVerifier::create)

        .assertNext(submissionDetailsDto ->
            assertThat(submissionDetailsDto.matchers())
                .isNotNull()
                .isNotEmpty()
                .containsKeys("contact", "registrationNumber")
                .containsEntry("contact", "00000000,00000001")
                .containsEntry("registrationNumber", "00000002")

        )

        .verifyComplete();
  }

  @Test
  @DisplayName("Should return empty SubmissionDetailsDto when submission id is invalid")
  void shouldNotGetReturnForInvalidId() {
    service
        .getSubmissionDetail(-1L)
        .as(StepVerifier::create)
        .verifyComplete();
  }

}
