package ca.bc.gov.app.core.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI().info(apiInfo());
  }

  private Info apiInfo() {
    return new Info().title("FSA Forest Client").description("Forest Client Application")
        .version("1.0");
  }
}