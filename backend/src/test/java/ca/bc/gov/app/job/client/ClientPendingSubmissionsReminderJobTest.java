package ca.bc.gov.app.job.client;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ca.bc.gov.app.configuration.ForestClientConfiguration;
import ca.bc.gov.app.dto.client.ClientSubmissionDistrictListDto;
import ca.bc.gov.app.service.ches.ChesService;
import ca.bc.gov.app.service.client.ClientSubmissionService;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@DisplayName("Unit Test | ClientPendingSubmissionsReminderJob")
class ClientPendingSubmissionsReminderJobTest {

    private ChesService chesService = mock(ChesService.class);
    private ClientSubmissionService clientSubmissionService = mock(ClientSubmissionService.class);
    private ForestClientConfiguration configuration = mock(ForestClientConfiguration.class);

    private ClientPendingSubmissionsReminderJob job;

    @BeforeEach
    void setup() {
        job = new ClientPendingSubmissionsReminderJob(
            chesService, 
            clientSubmissionService, 
            configuration);
    }

    @Test
    @DisplayName("Should send email for pending submissions")
    void shouldSendEmailForPendingSubmissions() {
        ClientSubmissionDistrictListDto item =
            new ClientSubmissionDistrictListDto(123L, "Test District", "test@example.com");
        String interval = "7 days";

        when(configuration.getSubmissionLimit()).thenReturn(Duration.ofDays(7));
        when(clientSubmissionService.pendingSubmissions()).thenReturn(Flux.just(item));
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
        )).thenReturn(Mono.just("EMAIL_OK"));

        job.startCheckingPendingSubmissionsJob();
        
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
