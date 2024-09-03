package ca.bc.gov.app.extensions;

import java.time.Duration;
import java.util.UUID;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.utility.DockerImageName;

public class CustomOracleContainer extends OracleContainer {

  public CustomOracleContainer() {
    super(
        DockerImageName
            .parse("gvenzl/oracle-free:23.5-slim-faststart")
            .asCompatibleSubstituteFor("gvenzl/oracle-xe")
    );

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
