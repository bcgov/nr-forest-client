package ca.bc.gov.app.extensions;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
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
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ContextConfiguration
public abstract class AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @Autowired
  private ObjectMapper objectMapper;

  static final PostgreSQLContainer database;

  /**
   * The auto-configured {@link WebTestClient} is created with {@code WebTestClient.bindToServer()}
   * because these tests boot on a random port. Unlike the application's own WebFlux/WebClient
   * codecs, this client does not apply {@code spring.http.codecs.preferred-json-mapper: jackson2}
   * and falls back to the Spring Boot 4 default Jackson 3 codecs when decoding response bodies.
   * The application is explicitly Jackson 2 based ({@code com.fasterxml.jackson.databind.*}), and
   * several DTOs (e.g. {@link ca.bc.gov.app.dto.ValidationError}) are plain classes with
   * {@code final} fields that only Jackson 2 can deserialize into non-null values. Align the test
   * client with the application's Jackson 2 {@link ObjectMapper} so decoded DTOs round-trip
   * correctly.
   */
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

  static {
    database = new PostgreSQLContainer("postgres:13")
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
