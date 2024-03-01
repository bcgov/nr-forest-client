package ca.bc.gov.app.security;

import ca.bc.gov.app.configuration.ForestClientConfiguration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity.CorsSpec;
import org.springframework.stereotype.Component;

/**
 * This class customizes the CORS (Cross-Origin Resource Sharing) configuration for the application.
 * It implements the Customizer interface and overrides the customize method to set the CORS
 * configuration source. The CORS configuration source is set to a function that returns a
 * CorsConfiguration object. The CorsConfiguration object is configured with the allowed origins,
 * methods, headers, exposed headers, max age, and allow credentials settings from the
 * ForestClientConfiguration.
 */
@RequiredArgsConstructor
@Component
public class ForestCorsCustomizer implements Customizer<CorsSpec> {

  /**
   * The configuration object that contains the settings for the CORS configuration.
   */
  private final ForestClientConfiguration configuration;

  /**
   * This method customizes the CORS configuration by setting the CORS configuration source. The
   * CORS configuration source is set to a function that returns a CorsConfiguration object. The
   * CorsConfiguration object is configured with the allowed origins, methods, headers, exposed
   * headers, max age, and allow credentials settings from the ForestClientConfiguration.
   *
   * @param corsSpec The CORS specification to be customized.
   */
  @Override
  public void customize(CorsSpec corsSpec) {
    corsSpec.configurationSource(
        serverWebExchange -> {
          var corsConfig = new org.springframework.web.cors.CorsConfiguration();
          var frontendConfig = configuration.getFrontend();
          var cors = frontendConfig.getCors();

          corsConfig.setAllowedOrigins(List.of(frontendConfig.getUrl()));
          corsConfig.setAllowedMethods(cors.getMethods());
          corsConfig.setAllowedHeaders(cors.getHeaders());
          corsConfig.setExposedHeaders(cors.getHeaders());
          corsConfig.setMaxAge(cors.getAge().getSeconds());
          corsConfig.setAllowCredentials(true);

          return corsConfig;
        }
    );
  }
}
