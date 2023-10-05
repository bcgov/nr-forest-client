package ca.bc.gov.app.configuration;

import ca.bc.gov.app.dto.SubmissionInformationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
@RegisterReflectionForBinding({
    SubmissionInformationDto.class,
    Integer.class,
    Message.class
})
public class GlobalServiceConfiguration {

  @Bean
  public WebClient forestClientApi(ForestClientConfiguration configuration) {
    return WebClient
        .builder()
        .baseUrl(configuration.getBackend().getUri())
        .build();
  }

}
