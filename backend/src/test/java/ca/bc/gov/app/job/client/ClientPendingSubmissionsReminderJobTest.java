package ca.bc.gov.app.job.client;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ca.bc.gov.app.dto.client.ClientSubmissionDistrictListDto;
import ca.bc.gov.app.service.ches.ChesService;
import ca.bc.gov.app.service.client.ClientSubmissionService;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
@DisplayName("Unit Test | ClientPendingSubmissionsReminderJob sendPendingSubmissionEmails")
public class ClientPendingSubmissionsReminderJobTest {

    private ChesService chesService = mock(ChesService.class);

    private ClientSubmissionService service;

    @BeforeEach
    void setup() {
      service = new ClientSubmissionService(
          null, 
          null, 
          null, 
          null, 
          null, 
          null, 
          null, 
          null,
          chesService, 
          null, 
          null, 
          null);
    }

    @Test
    @DisplayName("Should send email for pending submissions")
    void shouldSendEmailForPendingSubmissions() {
      
      ClientSubmissionDistrictListDto item =
          new ClientSubmissionDistrictListDto(123L, "Test District", "test@example.com");
      String interval = "7 days";

        when(chesService.sendEmail(
                eq("pendingSubmission"),
                eq(item.emails()),
                eq("New client number application pending for over seven days"),
                eq(Map.of(
                    "districtName", item.district(),
                    "submissionId", item.id(),
                    "interval", interval
                )),
                isNull()
        )).thenReturn(Mono.just("OK"));

        service.pendingSubmissions()
            .as(StepVerifier::create)
            .expectNext(item)
            .verifyComplete();

        verify(chesService).sendEmail(
            eq("pendingSubmission"),
            eq(item.emails()),
            eq("New client number application pending for over seven days"),
            eq(Map.of(
                "districtName", item.district(),
                "submissionId", item.id(),
                "interval", interval
            )),
            isNull()
        );
    }
    
}
