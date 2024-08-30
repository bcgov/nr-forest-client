package ca.bc.gov.app.extensions;

import java.time.Duration;
import java.util.UUID;
import org.testcontainers.containers.OracleContainer;

public class CustomOracleContainer extends OracleContainer {

  public CustomOracleContainer() {
    super("gvenzl/oracle-free:23.5-slim-faststart");

    this.withDatabaseName("legacyfsa")
        .withUsername("THE")
        .withPassword(UUID.randomUUID().toString().substring(24));
  }

  @Override
  protected void waitUntilContainerStarted() {
    getWaitStrategy()
        .withStartupTimeout(Duration.ofMinutes(10))
        .waitUntilReady(this);
  }

}
