package ca.bc.gov.app.configuration;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.util.LastItemReleaseStrategy;
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
  public FluxMessageChannel saveToLegacy() {
    return new FluxMessageChannel();
  }

  @Bean
  public FluxMessageChannel submissionMail() {
    return new FluxMessageChannel();
  }

  @Bean
  public FluxMessageChannel submissionCompletedChannel() {
    return new FluxMessageChannel();
  }

  @Bean
  public FluxMessageChannel notificationProcessingChannel() {
    return new FluxMessageChannel();
  }

  @Bean
  public FluxMessageChannel submissionCompletionChannel() {
    return new FluxMessageChannel();
  }


  @Bean
  public FluxMessageChannel submissionLegacyClientChannel() {
    return new FluxMessageChannel();
  }

  @Bean
  public FluxMessageChannel submissionLegacyLocationChannel() {
    return new FluxMessageChannel();
  }

  @Bean
  public FluxMessageChannel submissionLegacyContactChannel() {
    return new FluxMessageChannel();
  }

  @Bean
  public FluxMessageChannel submissionLegacyAggregateChannel() {
    return new FluxMessageChannel();
  }

  @Bean
  public FluxMessageChannel submissionLegacyNotifyChannel() {
    return new FluxMessageChannel();
  }

  @Bean
  public FluxMessageChannel submissionLegacyClientPersistChannel() {
    return new FluxMessageChannel();
  }

  @Bean
  public FluxMessageChannel submissionLegacyIndividualChannel() {
    return new FluxMessageChannel();
  }

  @Bean
  public FluxMessageChannel submissionLegacyUSPChannel() {
    return new FluxMessageChannel();
  }

  @Bean
  public FluxMessageChannel submissionLegacyRSPChannel() {
    return new FluxMessageChannel();
  }

  @Bean
  public FluxMessageChannel submissionLegacyOtherChannel() {
    return new FluxMessageChannel();
  }



  @Bean
  public R2dbcMessageSource submissionMessages(
      @Qualifier("clientR2dbcEntityOperations") R2dbcEntityTemplate r2dbcEntityTemplate
  ) {
    final String submissionIdQuery = """
        SELECT nrfc.submission.submission_id
        FROM nrfc.submission
        WHERE nrfc.submission.submission_type_code = 'SPP'""";

    R2dbcMessageSource template = new R2dbcMessageSource(
        r2dbcEntityTemplate,
        submissionIdQuery
    );
    template.setPayloadType(Integer.class);
    template.setExpectSingleResult(false);
    return template;
  }

  @Bean
  public R2dbcMessageSource processedMessage(
      @Qualifier("clientR2dbcEntityOperations") R2dbcEntityTemplate r2dbcEntityTemplate
  ) {
    final String submissionIdQuery = """
        SELECT nrfc.submission.submission_id
        FROM nrfc.submission
        LEFT JOIN nrfc.submission_matching_detail 
        ON nrfc.submission_matching_detail.submission_id = nrfc.submission.submission_id
        WHERE
        nrfc.submission.submission_status_code in ('R','A')
        AND ( nrfc.submission_matching_detail.submission_matching_processed  is null or
        nrfc.submission_matching_detail.submission_matching_processed = false)""";

    R2dbcMessageSource template = new R2dbcMessageSource(
        r2dbcEntityTemplate,
        submissionIdQuery
    );
    template.setPayloadType(Integer.class);
    template.setExpectSingleResult(false);
    return template;
  }

  @Bean
  public IntegrationFlow processingIntegrationFlow(
      @Value("${ca.bc.gov.nrs.processor.poolTime:1M}") Duration poolingTime,
      @Qualifier(ApplicationConstant.SUBMISSION_LIST_CHANNEL) FluxMessageChannel inputChannel,
      @Qualifier(ApplicationConstant.SUBMISSION_MESSAGE_SOURCE) R2dbcMessageSource messageSource
  ) {
    return IntegrationFlow
        .from(messageSource, adapter -> adapter.poller(Pollers.fixedDelay(poolingTime)))
        .split()
        .channel(inputChannel)
        .get();
  }

  @Bean
  public IntegrationFlow notifyingIntegrationFlow(
      @Value("${ca.bc.gov.nrs.processor.poolTime:1M}") Duration poolingTime,
      @Qualifier(ApplicationConstant.SUBMISSION_POSTPROCESSOR_CHANNEL) FluxMessageChannel inputChannel,
      @Qualifier(ApplicationConstant.PROCESSED_MESSAGE_SOURCE) R2dbcMessageSource messageSource
  ) {
    return IntegrationFlow
        .from(messageSource, adapter -> adapter.poller(Pollers.fixedDelay(poolingTime)))
        .split()
        .channel(inputChannel)
        .get();
  }

  @Bean
  public IntegrationFlow aggregateLegacyData(
      @Value("${ca.bc.gov.nrs.processor.poolTime:1M}") Duration poolingTime
  ){
    return
      IntegrationFlow
        .from("submissionLegacyAggregateChannel")
        .aggregate(spec ->
            spec
                .expireTimeout(poolingTime.toMillis())
                .releaseStrategy(new LastItemReleaseStrategy())
                .correlationStrategy(message -> message.getHeaders().get(ApplicationConstant.SUBMISSION_ID))
                .sendPartialResultOnExpiry(true)
        )
        .channel(submissionLegacyNotifyChannel())
        .get();
  }

}
