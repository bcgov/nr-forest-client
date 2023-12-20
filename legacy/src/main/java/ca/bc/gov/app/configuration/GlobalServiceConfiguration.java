package ca.bc.gov.app.configuration;

import ca.bc.gov.app.dto.ClientNameCodeDto;
import ca.bc.gov.app.dto.ForestClientDto;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RegisterReflectionForBinding({
    ClientNameCodeDto.class,
    ForestClientDto.class
})
public class GlobalServiceConfiguration {

  @Bean
  public WebClient forestClientApi(LegacyConfiguration configuration) {
    return WebClient.builder().baseUrl(configuration.getForest().getUri()).build();
  }

}
