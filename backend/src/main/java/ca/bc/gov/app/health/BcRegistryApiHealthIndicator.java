package ca.bc.gov.app.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BcRegistryApiHealthIndicator implements ManualHealthIndicator {

  private Health apiHealth = Health.up().withDetail("state","UP").build();

  @Override
  public Health health() {
    return apiHealth;
  }

  @Override
  public void isHealthy(boolean healthy, Throwable error) {
    Health.Builder healthBuilder = Health.up();
    if (!healthy) {
      healthBuilder =
          apiHealth.getStatus().getCode().equals("UP")
              ? Health.up().withDetail("state","UNKNOWN")
              : Health.up().withDetail("state","DOWN");
    }
    healthBuilder = error == null ? healthBuilder : healthBuilder.withException(error);
    apiHealth = healthBuilder.build();
  }

}
