package ca.bc.gov.app.configuration;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.dialect.PostgresDialect;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableR2dbcRepositories(
    basePackages = { "ca.bc.gov.app.repository.client" },
    entityOperationsRef = "clientR2dbcEntityOperations"
)
@EnableTransactionManagement
public class ClientDataConfiguration {

  @Bean
  @Qualifier(value = "clientConnectionFactory")
  public ConnectionFactory clientConnectionFactory(
      @Value("${ca.bc.gov.nrs.postgres.url}") String url,
      @Value("${ca.bc.gov.nrs.postgres.username}") String username,
      @Value("${ca.bc.gov.nrs.postgres.password}") String password
  ) {
    return ConnectionFactories.get(
        ConnectionFactoryOptions
            .parse(url)
            .mutate()
            .option(ConnectionFactoryOptions.USER, username)
            .option(ConnectionFactoryOptions.PASSWORD, password)
            .build()
    );
  }

  @Bean
  public R2dbcEntityOperations clientR2dbcEntityOperations(
      @Qualifier("clientConnectionFactory") ConnectionFactory connectionFactory
  ) {
    return new R2dbcEntityTemplate(
        DatabaseClient.create(connectionFactory),
        PostgresDialect.INSTANCE
    );
  }
}
