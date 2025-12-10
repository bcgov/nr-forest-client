package ca.bc.gov.app.health;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@Observed
public class ProcessorApiHealthIndicator implements HealthIndicator {

  private final WebClient processorApi;
  private final ObservationRegistry registry;
  private Health apiHealth = Health.up().build();

  public ProcessorApiHealthIndicator(
      @Qualifier("processorApi") WebClient processorApi,
      ObservationRegistry registry
  ) {
    this.processorApi = processorApi;
    this.registry = registry;
  }

  @Override
  public Health health() {
    processorApi
        .get()
        .uri("/health")
        .exchangeToMono(clientResponse -> {
          if (clientResponse.statusCode().is2xxSuccessful()) {
            return Mono.just(Health.up().withDetail("state", "UP").build());
          } else {
            return Mono.just(Health.up().withDetail("state", "DOWN").build());
          }
        })
        .name("request.processor")
        .tag("kind", "health")
        .tap(Micrometer.observation(registry))
        .subscribe(
            health -> apiHealth = health,
            error -> apiHealth = Health
                .up()
                .withDetail("state", "DOWN")
                .withDetail("error", error.getMessage())
                .withException(error)
                .build()
        );

    return apiHealth;
  }
}
