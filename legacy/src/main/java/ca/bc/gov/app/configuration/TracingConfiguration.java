package ca.bc.gov.app.configuration;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class TracingConfiguration {

  @Bean
  ObservedAspect observedAspect(
      ObservationRegistry observationRegistry
  ) {
    ObservationThreadLocalAccessor.getInstance().setObservationRegistry(observationRegistry);
    return new ObservedAspect(observationRegistry);
  }
}
