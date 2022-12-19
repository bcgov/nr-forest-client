package ca.bc.gov.app.configuration;

import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(
    entityManagerFactoryRef = "oracleEntityMgrFactory",
    transactionManagerRef = "oracleTransactionMgr",
    basePackages = {"ca.bc.gov.app.m.oracle.legacyclient.repository"}
)
@EnableTransactionManagement
public class OraclePersistenceConfiguration {

  @Bean(name = "oracleEntityMgrFactory")
  public LocalContainerEntityManagerFactoryBean oracleEntityMgrFactory(
      final EntityManagerFactoryBuilder builder,
      @Qualifier("oracleDatasource") final DataSource dataSource
  ) {
    return builder
        .dataSource(dataSource)
        .properties(new HashMap<>())
        .packages("ca.bc.gov.app.m.oracle.legacyclient.entity")
        .persistenceUnit("oracle")
        .build();
  }

  @Bean(name = "oracleDatasource")
  @ConfigurationProperties(prefix = "ca.bc.gov.datasource.oracle")
  public DataSource oracleDatasource() {
    return DataSourceBuilder.create().build();
  }

  @Bean(name = "oracleTransactionMgr")
  public PlatformTransactionManager oracleTransactionMgr(
      @Qualifier("oracleEntityMgrFactory") final EntityManagerFactory entityManagerFactory
  ) {
    return new JpaTransactionManager(entityManagerFactory);
  }

}