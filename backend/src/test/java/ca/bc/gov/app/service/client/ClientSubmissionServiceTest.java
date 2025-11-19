package ca.bc.gov.app.service.client;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ca.bc.gov.app.configuration.ForestClientConfiguration;
import ca.bc.gov.app.dto.client.ClientSubmissionDistrictListDto;
import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.repository.client.SubmissionRepository;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@Slf4j
@DisplayName("Integrated Test | FSA Client Submission Service")
class ClientSubmissionServiceTest extends AbstractTestContainerIntegrationTest {

  @Autowired
  private ClientSubmissionService service;
  
  @Autowired
  private SubmissionRepository submissionRepository;
  
  @Autowired
  private ForestClientConfiguration configuration;

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
  
  @Test
  @DisplayName("Should return pending submissions")
  void shouldReturnPendingSubmissions() {
      ClientSubmissionDistrictListDto testItem =
        new ClientSubmissionDistrictListDto(
            123L, 
            "Test District", 
            "test@example.com");

      when(configuration.getSubmissionLimit()).thenReturn(Duration.ofDays(7));

      String expectedInterval = configuration.getSubmissionLimit().toDays() + " days";

      when(submissionRepository.retrievePendingSubmissions(expectedInterval))
              .thenReturn(Flux.just(testItem));

      service.pendingSubmissions()
              .as(StepVerifier::create)
              .expectNext(testItem)
              .verifyComplete();

      verify(submissionRepository).retrievePendingSubmissions(expectedInterval);
  }

}
