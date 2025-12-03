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
public class LegacyApiHealthIndicator implements HealthIndicator {

  private final WebClient legacyApi;
  private final ObservationRegistry registry;
  private Health apiHealth = Health.unknown().build();

  public LegacyApiHealthIndicator(
      @Qualifier("legacyApi") WebClient legacyApi,
      ObservationRegistry registry
  ) {
    this.legacyApi = legacyApi;
    this.registry = registry;
  }

  @Override
  public Health health() {
    legacyApi
        .get()
        .uri("/health")
        .exchangeToMono(clientResponse -> {
          if (clientResponse.statusCode().is2xxSuccessful()) {
            return Mono.just(Health.up().withDetail("state", "UP").build());
          } else {
            return Mono.just(Health.up().withDetail("state", "DOWN").build());
          }
        })
        .name("request.legacy")
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
