package ca.bc.gov.app.configuration;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.annotation.DefaultNewSpanParser;
import io.micrometer.tracing.annotation.ImperativeMethodInvocationProcessor;
import io.micrometer.tracing.annotation.MethodInvocationProcessor;
import io.micrometer.tracing.annotation.NewSpanParser;
import io.micrometer.tracing.annotation.SpanAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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

  @Bean
  @ConditionalOnBean(Tracer.class)
  SpanAspect spanAspect(MethodInvocationProcessor methodInvocationProcessor) {
    return new SpanAspect(methodInvocationProcessor);
  }

  @Bean
  @ConditionalOnBean(Tracer.class)
  MethodInvocationProcessor methodInvocationProcessor(NewSpanParser newSpanParser, Tracer tracer,
      BeanFactory beanFactory) {
    return new ImperativeMethodInvocationProcessor(newSpanParser, tracer, beanFactory::getBean,
        beanFactory::getBean);
  }

  @Bean
  @ConditionalOnBean(Tracer.class)
  NewSpanParser newSpanParser() {
    return new DefaultNewSpanParser();
  }
}