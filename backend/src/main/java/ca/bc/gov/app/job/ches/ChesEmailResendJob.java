package ca.bc.gov.app.job.ches;

import ca.bc.gov.app.repository.client.EmailLogRepository;
import ca.bc.gov.app.service.ches.ChesService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service for resending failed emails. This service is scheduled to run at fixed intervals and
 * resends any emails that previously failed to send.
 */
@Service
@Slf4j
public class ChesEmailResendJob {

  private final EmailLogRepository emailLogRepository;
  private final ChesService chesService;
  private final Counter emailRetryCounter;

  /**
   * Constructs a new ChesEmailResendJob.
   *
   * @param emailLogRepository the repository for accessing email logs
   * @param chesService        the service for sending emails
   * @param meterRegistry      the registry for accessing application metrics
   */
  public ChesEmailResendJob(
      EmailLogRepository emailLogRepository,
      ChesService chesService,
      MeterRegistry meterRegistry
  ) {
    this.emailLogRepository = emailLogRepository;
    this.chesService = chesService;

    emailRetryCounter = meterRegistry.counter("service.ches", "status", "retry");
  }

  /**
   * Starts the job for resending failed emails. This method is scheduled to run at fixed intervals
   * and resends any emails that previously failed to send. For each failed email, it increments a
   * counter and then attempts to resend the email.
   */
  @Scheduled(fixedDelay = 30_000)
  public void startResendJob() {
    emailLogRepository
        .findByEmailSentInd("N")
        .doOnNext(emailLogEntity -> {
          log.info("Resending failed email with ID: {}", emailLogEntity.getEmailLogId());
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