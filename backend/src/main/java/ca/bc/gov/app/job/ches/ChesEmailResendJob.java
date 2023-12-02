package ca.bc.gov.app.job.ches;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import ca.bc.gov.app.repository.client.EmailLogRepository;
import ca.bc.gov.app.service.ches.ChesService;
import io.r2dbc.postgresql.codec.Json;

@Service
@Slf4j
public class ChesEmailResendJob {

  private final ScheduledExecutorService scheduler;
  private final EmailLogRepository emailLogRepository;
  private final ChesService chesService;
  private final Jackson2ObjectMapperBuilder builder;

  public ChesEmailResendJob(EmailLogRepository emailLogRepository,
                            ChesService chesService,
                            Jackson2ObjectMapperBuilder builder) {
    this.scheduler = Executors.newScheduledThreadPool(1);
    this.emailLogRepository = emailLogRepository;
    this.chesService = chesService;
    this.builder = builder;
  }

  public void startResendJob() {
    scheduler.scheduleAtFixedRate(() -> {
      emailLogRepository.findByEmailSentInd("N").doOnNext(emailLogEntity -> {
        log.info("Resending failed email with ID: " + emailLogEntity.getEmailLogId());

        Map<String, Object> emailVariables = convertFrom(emailLogEntity.getEmailVariables());
        chesService.sendEmail(emailLogEntity.getTemplateName(), 
                              emailLogEntity.getEmailAddress(),
                              emailLogEntity.getEmailSubject(), 
                              emailVariables,
                              emailLogEntity.getEmailLogId()
                              ).subscribe();
      }).subscribe();
    }, 0, 1, TimeUnit.HOURS);

  }

  public void stopScheduler() {
    scheduler.shutdown();
  }
  
  @SuppressWarnings({"unchecked", "deprecation"})
  private Map<String, Object> convertFrom(Json jsonValue) {
    String json = StringUtils.defaultString(jsonValue.asString(), "{}");

    try {
      return builder.build().readValue(json, Map.class);
    } catch (JsonProcessingException e) {
      log.error("Error while converting json to map", e);
    }

    return Map.of();
  }

}
