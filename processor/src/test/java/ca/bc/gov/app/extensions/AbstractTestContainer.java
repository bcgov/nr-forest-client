package ca.bc.gov.app.extensions;

import java.io.IOException;
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

  static {
    Path finalFile = Paths.get("./src", "test", "resources", "init_pg.sql").normalize();

    //File used to initialize things on postgres container
    Path init = Paths.get("./src", "test", "resources", "postgres", "init.sql").normalize();
    //Test specific content
    Path tests = Paths.get("./src", "test", "resources", "postgres", "test.sql").normalize();
    //Backend related scripts folder
    Path backendFolder =
        Paths.get("../backend", "src", "main", "resources", "db", "migration").normalize();

    if (!finalFile.toFile().exists()) {
      try {
        finalFile.toFile().createNewFile();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    try {

      Stream<Path> initStream = Stream.of(init);
      Stream<Path> testStream = Stream.of(tests);
      Stream<Path> backEndStream = Files.list(backendFolder).map(Path::normalize);

      Stream<Path> finalStream = Stream
          .concat(Stream.concat(initStream, backEndStream), testStream);

      Files
          .write(
              finalFile,
              finalStream
                  .filter(path -> path.toFile().exists())
                  .map(AbstractTestContainer::readAll)
                  .flatMap(List::stream)
                  .toList(),
              StandardOpenOption.TRUNCATE_EXISTING
          );

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static {

    Path finalFile = Paths.get("./src", "test", "resources", "db", "migration",
        "V1__init_oracle.sql").normalize();

    if (!finalFile.toFile().getParentFile().exists()) {
      finalFile.toFile().getParentFile().mkdirs();
    } else {
      try {
        Files.list(finalFile.toFile().getParentFile().toPath())
            .forEach(path -> {
              try {
                Files.delete(path);
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    Path legacyFolder =
        Paths.get("../legacy", "src", "test", "resources", "db", "migration").normalize();

    try {
      Stream<Path> legacyStream = Files
          .list(legacyFolder)
          .map(Path::normalize);

      // copy the files from legacyStream into the same folder as the finalFile
      legacyStream
          .forEach(path -> {
            try {
              Files.copy(path, finalFile.resolveSibling(path.getFileName()));
            } catch (IOException e) {
              e.printStackTrace();
            }
          });


    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  static final OracleContainer oracle;
  static final PostgreSQLContainer postgres;

  static {
    postgres = (PostgreSQLContainer) new PostgreSQLContainer("postgres:13")
        .withInitScript("init_pg.sql")
        .withDatabaseName("nfrc")
        .withUsername("nrfc")
        .withPassword(genPassword());

    postgres.start();
  }


  static {

    oracle = new OracleContainer("gvenzl/oracle-xe:21-slim-faststart")
        .withDatabaseName("legacyfsa")
        .withUsername("THE")
        .withPassword(genPassword());
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

  @SneakyThrows
  private static List<String> readAll(Path path) {
    return Files.readAllLines(path);
  }


}
