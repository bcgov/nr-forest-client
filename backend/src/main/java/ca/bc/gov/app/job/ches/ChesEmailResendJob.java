package ca.bc.gov.app.job.ches;

import ca.bc.gov.app.repository.client.EmailLogRepository;
import ca.bc.gov.app.service.ches.ChesService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChesEmailResendJob {

  private final EmailLogRepository emailLogRepository;
  private final ChesService chesService;
  private final Counter emailRetryCounter;

  public ChesEmailResendJob(
      EmailLogRepository emailLogRepository,
      ChesService chesService,
      MeterRegistry meterRegistry
  ) {
    this.emailLogRepository = emailLogRepository;
    this.chesService = chesService;

    emailRetryCounter = meterRegistry.counter("service.ches", "status", "retry");
  }

  //@Scheduled(cron = "* */3 * * * *")
  public void startResendJob() {

    log.info("Starting email resend job");

    emailLogRepository
        .findByEmailSentInd("N")
        .doOnNext(emailLogEntity -> {
          log.info("Resending failed email with ID: " + emailLogEntity.getEmailLogId());
          emailRetryCounter.increment();
        })
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
