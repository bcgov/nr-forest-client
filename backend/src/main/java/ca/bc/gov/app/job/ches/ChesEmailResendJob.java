package ca.bc.gov.app.job.ches;

import ca.bc.gov.app.repository.client.EmailLogRepository;
import ca.bc.gov.app.service.ches.ChesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChesEmailResendJob {

  private final EmailLogRepository emailLogRepository;
  private final ChesService chesService;

  @Scheduled(cron = "0 0 * * * *")
  public void startResendJob() {
    emailLogRepository
        .findByEmailSentInd("N")
        .doOnNext(emailLogEntity ->
            log.info("Resending failed email with ID: " + emailLogEntity.getEmailLogId())
        )
        .flatMap(emailLogEntity -> chesService.sendEmail(
              emailLogEntity.getTemplateName(),
              emailLogEntity.getEmailAddress(),
              emailLogEntity.getEmailSubject(),
              emailLogEntity.getVariables(),
              emailLogEntity.getEmailLogId()
            )
        ).subscribe();
  }

}
