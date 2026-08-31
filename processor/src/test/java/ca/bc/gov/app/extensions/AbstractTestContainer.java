package ca.bc.gov.app.extensions;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ExtendWith({SpringExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ContextConfiguration
public abstract class AbstractTestContainer {

  @Autowired
  protected WebTestClient client;

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * The auto-configured {@link WebTestClient} is created with {@code WebTestClient.bindToServer()}
   * because these tests boot on a random port. Unlike the application's own WebFlux/WebClient
   * codecs, this client does not apply {@code spring.http.codecs.preferred-json-mapper: jackson2}
   * and falls back to the Spring Boot 4 default Jackson 3 codecs when decoding response bodies.
   * The application is explicitly Jackson 2 based ({@code com.fasterxml.jackson.*}). Align the
   * test client with the application's Jackson 2 {@link ObjectMapper} so decoded DTOs round-trip
   * correctly.
   *
   * <p>The {@code jackson2JsonEncoder}/{@code jackson2JsonDecoder} APIs used below are deprecated
   * for removal in Spring Framework 7 in favor of the Jackson 3-based {@code jacksonJsonEncoder}/
   * {@code jacksonJsonDecoder}. They are intentionally retained here as the sanctioned Jackson 2
   * compatibility bridge (see {@code spring-boot-jackson2}) until the module's DTOs/entities are
   * migrated off the classic Jackson 2 API. Suppressed rather than removed.
   */
  @SuppressWarnings({"removal", "java:S1874"})
  @BeforeEach
  public void configureJackson2Codecs() {
    client =
        client
            .mutate()
            .exchangeStrategies(
                ExchangeStrategies
                    .builder()
                    .codecs(
                        configurer -> {
                          configurer.defaultCodecs().jackson2JsonEncoder(
                              new Jackson2JsonEncoder(objectMapper));
                          configurer.defaultCodecs().jackson2JsonDecoder(
                              new Jackson2JsonDecoder(objectMapper));
                        })
                    .build())
            .build();
  }

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
            () -> postgres
                .getDatabaseName()
                .concat("?TC_INITSCRIPT=file:../backend/src/test/resources/init_pg.sql")
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