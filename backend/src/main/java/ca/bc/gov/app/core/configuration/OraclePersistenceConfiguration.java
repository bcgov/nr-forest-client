package ca.bc.gov.app.core.configuration;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = "oracleEntityManager",
    basePackages = "ca.bc.gov.app.m.oracle")
public class OraclePersistenceConfiguration {

  @Bean(name = "oracleDataSource")
  @ConfigurationProperties(prefix = "oracle.datasource")
  public DataSource dataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean(name = "oracleEntityManager")
  public LocalContainerEntityManagerFactoryBean oracleEntityManager(
      final EntityManagerFactoryBuilder builder,
      @Qualifier("oracleDataSource") final DataSource dataSource) {
    return builder.dataSource(dataSource)
        .packages("ca.bc.gov.app.m.oracle")
        .persistenceUnit("oracle")
        .build();
  }

}