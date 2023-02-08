package ca.bc.gov.app.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebfluxConfiguration implements WebFluxConfigurer {


  public void addCorsMappings(
      CorsRegistry registry,
      @Value("${ca.bc.gov.nrs.frontend.url}") String frontendUrl
  ) {
    registry.addMapping("/api/**")
        .allowedOrigins(frontendUrl)
        .allowedMethods("GET", "OPTIONS", "POST", "PUT", "DELETE")
        .maxAge(3600);

  }
}
