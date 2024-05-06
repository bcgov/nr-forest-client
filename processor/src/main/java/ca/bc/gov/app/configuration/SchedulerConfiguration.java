package ca.bc.gov.app.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Configuration
@ConditionalOnProperty(value = "ca.bc.gov.nrs.processor.scheduling", havingValue = "true", matchIfMissing = true)
public class SchedulerConfiguration {

}
