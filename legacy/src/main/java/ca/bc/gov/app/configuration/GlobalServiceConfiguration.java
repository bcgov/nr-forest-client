package ca.bc.gov.app.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GlobalServiceConfiguration {

  /**
   * Creates and configures a WebClient instance for accessing OrgBook API based on the provided
   * {@link LegacyConfiguration}.
   *
   * @param configuration The {@link LegacyConfiguration} containing the OrgBook API URI.
   * @return A {@link WebClient} instance configured for accessing OrgBook API.
   */
  @Bean
  public WebClient orgBookApi(LegacyConfiguration configuration) {
    return WebClient.builder().baseUrl(configuration.getOrgbook()).build();
  }

  @Bean
  public WebClient forestClientApi(LegacyConfiguration configuration) {
    return WebClient.builder().baseUrl(configuration.getForest().getUri()).build();
  }

}
