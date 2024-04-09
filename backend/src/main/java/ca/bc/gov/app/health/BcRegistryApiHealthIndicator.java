package ca.bc.gov.app.health;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import java.util.Map;
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
public class BcRegistryApiHealthIndicator implements HealthIndicator {

  private final WebClient addressCompleteApi;
  private final ObservationRegistry registry;
  private Health apiHealth = Health.unknown().build();

  public BcRegistryApiHealthIndicator(
      @Qualifier("bcRegistryApi") WebClient addressCompleteApi,
      ObservationRegistry registry
  ) {
    this.addressCompleteApi = addressCompleteApi;
    this.registry = registry;
  }

  @Override
  public Health health() {
    addressCompleteApi
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/registry-search/api/v1/businesses/search/suggest")
                .queryParam("query", "value:XX000000")
                .build(Map.of())
        )
        .exchangeToMono(clientResponse -> {
          if (clientResponse.statusCode().is2xxSuccessful()) {
            return Mono.just(Health.up().build());
          } else {
            return Mono.just(Health.down().build());
          }
        })
        .name("request.bcregistry")
        .tag("kind", "health")
        .tap(Micrometer.observation(registry))
        .doOnNext(health -> log.info("BC Registry API health: {}", health))
        .subscribe(
            health -> apiHealth = health,
            error -> apiHealth = Health.down().withException(error).build()
        );
    log.info("Checking Bc Registry API health");
    return apiHealth;
  }
}
