package ca.bc.gov.app.health;

import ca.bc.gov.app.configuration.ForestClientConfiguration;
import ca.bc.gov.app.dto.ches.CommonExposureJwtDto;
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
import reactor.core.publisher.Mono;

@Component
@Slf4j
@Observed
public class ChesHealthIndicator implements HealthIndicator {

  private final WebClient chesApi;

  private final WebClient authApi;
  private Health apiHealth = Health.unknown().build();

  public ChesHealthIndicator(
      @Qualifier("chesApi") WebClient chesApi,
      @Qualifier("authApi") WebClient authApi
  ) {
    this.chesApi = chesApi;
    this.authApi = authApi;
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
                    return Mono.just(Health.up().build());
                  } else {
                    return Mono.just(Health.down().build());
                  }
                })
        )
        .doOnNext(health -> log.info("CHES API health: {}", health))
        .subscribe(
            health -> apiHealth = health,
            error -> apiHealth = Health.down().withException(error).build()
        );
    log.info("Checking CHES API health");
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
            .map(CommonExposureJwtDto::accessToken);
  }
}
