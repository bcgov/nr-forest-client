package ca.bc.gov.app.extensions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ExtendWith({SpringExtension.class})
@SpringBootTest
@ContextConfiguration
public abstract class AbstractTestContainer {

  static final PostgreSQLContainer postgres;

  static {
    postgres = new PostgreSQLContainer("postgres:13")
        .withDatabaseName("nfrc")
        .withUsername("nrfc")
        .withPassword(genPassword());

    postgres.start();
  }

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {

    registry
        .add(
            "ca.bc.gov.nrs.postgres.database",
            postgres::getDatabaseName
        );

    registry
        .add(
            "ca.bc.gov.nrs.postgres.host",
            () -> String.format("%s:%d", postgres.getHost(), postgres.getMappedPort(5432))
        );

    registry
        .add(
            "ca.bc.gov.nrs.postgres.username",
            postgres::getUsername
        );

    registry
        .add(
            "ca.bc.gov.nrs.postgres.password",
            postgres::getPassword
        );
  }

  @NotNull
  private static String genPassword() {
    return UUID
        .randomUUID()
        .toString()
        .replace("-", "")
        .substring(24);
  }


}
