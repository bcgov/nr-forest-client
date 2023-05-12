package ca.bc.gov.app.extensions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ExtendWith({SpringExtension.class})
@SpringBootTest
@ContextConfiguration
public abstract class AbstractTestContainer {

  static final OracleContainer oracle;
  static final PostgreSQLContainer postgres;

  static {
    oracle = new OracleContainer("gvenzl/oracle-xe:21.3.0-slim-faststart")
        .withDatabaseName("legacyfsa")
        .withUsername("THE")
        .withPassword(genPassword());

    generateInitPostgres();

    postgres = (PostgreSQLContainer) new PostgreSQLContainer("postgres")
        .withInitScript("init_pg.sql")
        .withDatabaseName("nfrc")
        .withUsername("nrfc")
        .withPassword(genPassword());

    postgres.start();
    oracle.start();
  }

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    postgresSetup(registry);
    oracleSetup(registry);
  }

  @SneakyThrows
  private static void oracleSetup(DynamicPropertyRegistry registry) {
    registry
        .add(
            "ca.bc.gov.nrs.oracle.database",
            oracle::getDatabaseName
        );
    registry
        .add(
            "ca.bc.gov.nrs.oracle.service",
            oracle::getDatabaseName
        );

    registry
        .add(
            "ca.bc.gov.nrs.oracle.host",
            oracle::getHost);

    registry
        .add(
            "ca.bc.gov.nrs.oracle.port",
            () -> oracle.getMappedPort(1521));

    registry
        .add(
            "ca.bc.gov.nrs.oracle.username",
            oracle::getUsername
        );
    registry
        .add(
            "ca.bc.gov.nrs.oracle.schema",
            oracle::getUsername
        );

    registry
        .add(
            "ca.bc.gov.nrs.oracle.password",
            oracle::getPassword
        );

  }

  @SneakyThrows
  private static void postgresSetup(DynamicPropertyRegistry registry) {
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

  private static String getPath(String path) {
    return Paths.get(path).normalize().toFile().getAbsolutePath();
  }

  @SneakyThrows
  private static void generateInitPostgres() {
    Path finalFile = Paths.get("./src", "test", "resources", "init_pg.sql").normalize();

    //File used to initialize things on postgres container
    Path init = Paths.get("./src", "test", "resources", "postgres", "init.sql").normalize();
    //Test specific content
    Path tests = Paths.get("./src", "test", "resources", "postgres", "test.sql").normalize();
    //Backend related scripts folder
    Path backendFolder =
        Paths.get("../backend", "src", "main", "resources", "db", "migration").normalize();

    if (!finalFile.toFile().exists()) {
      finalFile.toFile().createNewFile();
    }

    Stream<Path> initStream = Stream.of(init);
    Stream<Path> testStream = Stream.of(tests);
    Stream<Path> backEndStream = Files.list(backendFolder).map(Path::normalize);

    Stream<Path> finalStream = Stream.concat(Stream.concat(initStream, backEndStream), testStream);


    Files
        .write(
            finalFile,
            finalStream
                .peek(path -> System.out.println("Going trough " + path))
                .map(AbstractTestContainer::readAll)
                .flatMap(List::stream)
                .toList(),
            StandardOpenOption.TRUNCATE_EXISTING
        );


  }

  @SneakyThrows
  private static List<String> readAll(Path path) {
    return Files.readAllLines(path);
  }

}
