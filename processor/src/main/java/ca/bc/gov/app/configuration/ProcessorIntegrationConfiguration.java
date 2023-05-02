package ca.bc.gov.app.configuration;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.r2dbc.inbound.R2dbcMessageSource;

@Configuration
public class ProcessorIntegrationConfiguration {


  // Starts here
  @Bean
  public FluxMessageChannel submissionListChannel() {
    return new FluxMessageChannel();
  }

  //Mandatory, check for matches
  @Bean
  public FluxMessageChannel matchCheckingChannel() {
    return new FluxMessageChannel();
  }

  //If no match and stance is null
  @Bean
  public FluxMessageChannel goodStanceChannel() {
    return new FluxMessageChannel();
  }

  //If no match and stance is true
  @Bean
  public FluxMessageChannel autoApproveChannel() {
    return new FluxMessageChannel();
  }

  //Everything else that is not the above
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
      @Qualifier("submissionListChannel") FluxMessageChannel inputChannel,
      R2dbcMessageSource messageSource
  ) {
    return IntegrationFlow
        .from(messageSource, c -> c.poller(Pollers.fixedDelay(Duration.ofMinutes(1))))
        .split()
        .channel(inputChannel)
        .get();
  }

}
