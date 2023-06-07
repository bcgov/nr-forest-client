package ca.bc.gov.app.configuration;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.dialect.OracleDialect;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableR2dbcRepositories(
    basePackages = { "ca.bc.gov.app.repository.legacy" },
    entityOperationsRef = "legacyR2dbcEntityOperations"
)
@EnableTransactionManagement
public class LegacyDataConfiguration {
  @Bean
  @Primary
  @Qualifier(value = "legacyConnectionFactory")
  public ConnectionFactory legacyConnectionFactory(
      @Value("${spring.r2dbc.url}") String url,
      @Value("${spring.r2dbc.username}") String username,
      @Value("${spring.r2dbc.password}") String password
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
  @Primary
  public R2dbcEntityOperations legacyR2dbcEntityOperations(
      @Qualifier("legacyConnectionFactory") ConnectionFactory connectionFactory
  ) {
    return new R2dbcEntityTemplate(
        DatabaseClient.create(connectionFactory),
        OracleDialect.INSTANCE
    );
  }
}
