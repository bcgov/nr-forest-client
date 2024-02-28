package ca.bc.gov.app.configuration;

import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import ca.bc.gov.app.dto.SubmissionInformationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
@RegisterReflectionForBinding({
    SubmissionInformationDto.class,
    Integer.class
})
public class GlobalServiceConfiguration {

  @Bean
  public WebClient forestClientApi(ForestClientConfiguration configuration) {
    return WebClient
        .builder()
        .baseUrl(configuration.getBackend().getUri())
        .filter(
            basicAuthentication(
                configuration.getSecurity().getServiceAccountName(),
                configuration.getSecurity().getServiceAccountSecret()
            )
        )
        .build();
  }

  @Bean
  public WebClient legacyClientApi(ForestClientConfiguration configuration) {
    return WebClient
        .builder()
        .baseUrl(configuration.getLegacy().getUri())
        .build();
  }

  /**
   * Returns a configured instance of WebClient for accessing the BC Registry API.
   *
   * @param configuration The configuration for the ForestClient.
   * @return A configured instance of WebClient for accessing the BC Registry API.
   */
  @Bean
  public WebClient bcRegistryApi(ForestClientConfiguration configuration) {
    return WebClient
        .builder()
        .baseUrl(configuration.getBcregistry().getUri())
        .defaultHeader("x-apikey", configuration.getBcregistry().getApiKey())
        .defaultHeader("Account-Id", configuration.getBcregistry().getAccountId())
        .build();
  }

}
