package ca.bc.gov.app.core.configuration;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = "postgresEntityManager",
    basePackages = "ca.bc.gov.app.m.postgres")
public class PostgresPersistenceConfiguration {

  public final static String POSTGRES_ATTRIBUTE_SCHEMA_QUALIFIER = "nrfc.";
  public final static String POSTGRES_ATTRIBUTE_SCHEMA = "nrfc";
  public static final String POSTGRES_API_TAG = "Client";

  @Primary
  @Bean(name = "postgresDataSource")
  @ConfigurationProperties(prefix = "postgres.datasource")
  public DataSource dataSource() {
    return DataSourceBuilder.create().build();
  }

  @Primary
  @Bean(name = "postgresEntityManager")
  public LocalContainerEntityManagerFactoryBean postgresEntityManager(
      final EntityManagerFactoryBuilder builder,
      @Qualifier("postgresDataSource") final DataSource dataSource) {
    return builder.dataSource(dataSource)
        .packages("ca.bc.gov.app.m.postgres")
        .persistenceUnit("postgres")
        .build();
  }

}