package ca.bc.gov.app.configuration;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.reactive.server.WebTestClient;

@Configuration
public class TestConfiguration {

  @Bean
  public WebTestClient webTestClient(ApplicationContext applicationContext) {
    return WebTestClient
        .bindToApplicationContext(applicationContext)
        .apply(springSecurity())
        .configureClient()
        .build();
  }

 /* @Bean
  public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
    http.authorizeExchange().anyExchange().permitAll();
    return http.build();
  }*/

}
