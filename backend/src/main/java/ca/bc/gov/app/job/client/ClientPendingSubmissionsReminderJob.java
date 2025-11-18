package ca.bc.gov.app.job.client;

import ca.bc.gov.app.configuration.ForestClientConfiguration;
import ca.bc.gov.app.dto.client.ClientSubmissionDistrictListDto;
import ca.bc.gov.app.service.ches.ChesService;
import ca.bc.gov.app.service.client.ClientSubmissionService;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ClientPendingSubmissionsReminderJob implements SchedulingConfigurer {

  private final ChesService chesService;
  private final ClientSubmissionService clientSubmissionService;
  private final ForestClientConfiguration configuration;

  public ClientPendingSubmissionsReminderJob(
      ChesService chesService,
      ClientSubmissionService clientService,
      ForestClientConfiguration configuration
  ) {
    this.chesService = chesService;
    this.clientSubmissionService = clientService;
    this.configuration = configuration;
  }

  @Override
  public void configureTasks(ScheduledTaskRegistrar registrar) {
    Duration interval = configuration.getSubmissionLimit();

    PeriodicTrigger trigger = new PeriodicTrigger(interval);
    trigger.setFixedRate(false);

    registrar.addTriggerTask(
        this::startCheckingPendingSubmissionsJob,
        trigger
    );
  }

  public void startCheckingPendingSubmissionsJob() {
    log.info("Running pending submissions job...");
    long days = configuration.getSubmissionLimit().toDays();
    String interval = days + " days";
    
    clientSubmissionService.pendingSubmissions()
      .flatMap(item -> {
          log.info("Sending email for item {}", item);
          return chesService.sendEmail(
              "pendingSubmission", 
              item.emails(),
              "New client number application pending for over seven days",
              emailParameters(item, interval), 
              null
          );
      })
      .doOnNext(emailId -> log.info("Email sent, transaction ID: {}", emailId))
      .doOnError(e -> log.error("Error sending email", e))
      .subscribe();
  }
  
  private Map<String, Object> emailParameters(
      ClientSubmissionDistrictListDto clientSubmissionDistrictListDto,
      String interval
  ) {
    return Map.of(
        "districtName", clientSubmissionDistrictListDto.district(),
        "submissionId", clientSubmissionDistrictListDto.id(),
        "interval", interval
    );
  }

}
