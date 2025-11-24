package ca.bc.gov.app.health;

import ca.bc.gov.app.dto.ches.CommonExposureJwtDto;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@Observed
public class ChesHealthIndicator implements HealthIndicator {

  private final WebClient chesApi;
  private final WebClient authApi;
  private final ObservationRegistry registry;
  private Health apiHealth = Health.unknown().build();

  public ChesHealthIndicator(
      @Qualifier("chesApi") WebClient chesApi,
      @Qualifier("authApi") WebClient authApi,
      ObservationRegistry registry
  ) {
    this.chesApi = chesApi;
    this.authApi = authApi;
    this.registry = registry;
  }

  @Override
  public Health health() {
    getToken()
        .flatMap(token ->
            chesApi
                .get()
                .uri("/health")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeToMono(clientResponse -> {
                  if (clientResponse.statusCode().is2xxSuccessful()) {
                    return Mono.just(Health.up().withDetail("state", "UP").build());
                  } else {
                    return Mono.just(Health.up().withDetail("state", "DOWN").build());
                  }
                })
        )
        .name("request.ches")
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

  private Mono<String> getToken() {
    return
        authApi
            .post()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
            .retrieve()
            .bodyToMono(CommonExposureJwtDto.class)
            .name("request.ches")
            .tag("kind", "token")
            .tap(Micrometer.observation(registry))
            .map(CommonExposureJwtDto::accessToken);
  }
}
