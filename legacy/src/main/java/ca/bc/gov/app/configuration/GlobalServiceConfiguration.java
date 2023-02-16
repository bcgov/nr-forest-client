package ca.bc.gov.app.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GlobalServiceConfiguration {

  @Bean
  public WebClient forestClientApi(ForestConfiguration configuration) {
    return WebClient.builder().baseUrl(configuration.getUri()).build();
  }

}
