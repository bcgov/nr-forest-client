package ca.bc.gov.app.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class GlobalServiceConfiguration {

  @Bean
  public WebClient forestClientApi(ForestClientConfiguration configuration) {
    return WebClient
        .builder()
        .baseUrl(configuration.getBackend().getUri())
        .build();
  }

}
