package ca.bc.gov.app.health;

import ca.bc.gov.app.configuration.ForestClientConfiguration;
import io.micrometer.observation.annotation.Observed;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@Observed
public class CanadaPostApiHealthIndicator implements HealthIndicator {

  private final WebClient addressCompleteApi;
  private final ForestClientConfiguration.AddressCompleteConfiguration configuration;
  private Health apiHealth = Health.unknown().build();

  public CanadaPostApiHealthIndicator(
      ForestClientConfiguration configuration,
      @Qualifier("addressCompleteApi") WebClient addressCompleteApi
  ) {
    this.configuration = configuration.getAddressComplete();
    this.addressCompleteApi = addressCompleteApi;
  }

  @Override
  public Health health() {
    addressCompleteApi
        .get()
        .uri(uriBuilder ->
            uriBuilder
                .path("/find/v2.10/json3.ws")
                .queryParam("key", configuration.getApiKey())
                .build(Map.of())
        )
        .exchangeToMono(clientResponse -> {
          if (clientResponse.statusCode().is2xxSuccessful()) {
            return Mono.just(Health.up().build());
          } else {
            return Mono.just(Health.down().build());
          }
        })
        .doOnNext(health -> log.info("Canada Post API health: {}", health))
        .subscribe(
            health -> apiHealth = health,
            error -> apiHealth = Health.down().withException(error).build()
        );
    log.info("Checking Canada Post API health");


    return apiHealth;
  }
}