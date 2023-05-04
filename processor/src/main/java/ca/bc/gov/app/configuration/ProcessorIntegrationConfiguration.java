package ca.bc.gov.app.configuration;

import ca.bc.gov.app.ApplicationConstant;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.r2dbc.inbound.R2dbcMessageSource;

@Configuration
public class ProcessorIntegrationConfiguration {

  @Bean
  public FluxMessageChannel submissionListChannel() {
    return new FluxMessageChannel();
  }

  @Bean
  public FluxMessageChannel matchCheckingChannel() {
    return new FluxMessageChannel();
  }

  @Bean
  public FluxMessageChannel forwardChannel() {
    return new FluxMessageChannel();
  }

  @Bean
  public FluxMessageChannel autoApproveChannel() {
    return new FluxMessageChannel();
  }

  @Bean
  public FluxMessageChannel reviewChannel() {
    return new FluxMessageChannel();
  }

  @Bean
  public R2dbcMessageSource submissionMessages(
      @Qualifier("clientR2dbcEntityOperations") R2dbcEntityTemplate r2dbcEntityTemplate
  ) {
    final String submissionIdQuery = """
        SELECT nrfc.submission.submission_id
        FROM nrfc.submission
        WHERE nrfc.submission.submission_status_code = 'S'""";

    R2dbcMessageSource template = new R2dbcMessageSource(
        r2dbcEntityTemplate,
        submissionIdQuery
    );
    template.setPayloadType(Integer.class);
    template.setExpectSingleResult(false);
    return template;
  }

  @Bean
  public IntegrationFlow integrationFlow(
      @Value("${ca.bc.gov.nrs.processor.poolTime:1M}") Duration poolingTime,
      @Qualifier(ApplicationConstant.SUBMISSION_LIST_CHANNEL) FluxMessageChannel inputChannel,
      R2dbcMessageSource messageSource
  ) {
    return IntegrationFlow
        .from(messageSource, adapter -> adapter.poller(Pollers.fixedDelay(poolingTime)))
        .split()
        .channel(inputChannel)
        .get();
  }

}
