package ca.bc.gov.app.core.configuration;

import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(
    entityManagerFactoryRef = "postgresEntityMgrFactory",
    transactionManagerRef = "postgresTransactionMgr",
    basePackages = {"ca.bc.gov.app.m.postgres.client.repository"}
)
@EnableTransactionManagement
public class PostgresPersistenceConfiguration {

  @Bean(name = "postgresEntityMgrFactory")
  @Primary
  public LocalContainerEntityManagerFactoryBean postgresEntityMgrFactory(
      final EntityManagerFactoryBuilder builder,
      @Qualifier("postgresDatasource") final DataSource dataSource
  ) {
    return builder
        .dataSource(dataSource)
        .properties(new HashMap<>())
        .packages("ca.bc.gov.app.m.postgres.client.entity")
        .persistenceUnit("postgres")
        .build();
  }

  @Bean(name = "postgresDatasource")
  @ConfigurationProperties(prefix = "ca.bc.gov.datasource.postgres")
  @Primary
  public DataSource postgresDatasource() {
    return DataSourceBuilder.create().build();
  }

  @Bean(name = "postgresTransactionMgr")
  @Primary
  public PlatformTransactionManager postgresTransactionMgr(
      @Qualifier("postgresEntityMgrFactory") final EntityManagerFactory entityManagerFactory
  ) {
    return new JpaTransactionManager(entityManagerFactory);
  }

}