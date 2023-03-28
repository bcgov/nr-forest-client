package ca.bc.gov.app.extensions;

import java.util.UUID;
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
public abstract class AbstractTestContainerIntegrationTest {

  static final PostgreSQLContainer database;

  static {
    database = new PostgreSQLContainer("postgres")
        .withDatabaseName("simple")
        .withUsername("simple")
        .withPassword(UUID.randomUUID().toString());
    database.start();
  }

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {

    registry
        .add(
            "ca.bc.gov.nrs.postgres.database",
            () -> database
                .getDatabaseName()
                .concat("?TC_INITSCRIPT=file:src/test/resources/init_pg.sql")
        );

    registry
        .add(
            "ca.bc.gov.nrs.postgres.host",
            () -> String.format("%s:%d", database.getHost(), database.getMappedPort(5432))
        );

    registry
        .add(
            "ca.bc.gov.nrs.postgres.username",
            database::getUsername
        );

    registry
        .add(
            "ca.bc.gov.nrs.postgres.password",
            database::getPassword
        );
  }
}
