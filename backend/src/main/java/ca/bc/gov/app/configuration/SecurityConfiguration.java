package ca.bc.gov.app.configuration;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.security.ApiAuthorizationCustomizer;
import ca.bc.gov.app.security.CorsCustomizer;
import ca.bc.gov.app.security.CsrfCustomizer;
import ca.bc.gov.app.security.HeadersCustomizer;
import ca.bc.gov.app.security.Oauth2Customizer;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.WebFilter;

/**
 * This class configures the security settings for the application. It uses the @Configuration
 * annotation to indicate that it is a configuration class. It uses the
 * @EnableWebFluxSecurity annotation to enable Spring Security's reactive features.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

  /**
   * This method is a Spring Bean that configures the Spring Security filter chain.
   * The filter chain is a mechanism that Spring Security uses to apply security features
   * to HTTP requests.
   *
   * @param http The ServerHttpSecurity instance used to build the security filter chain.
   * @param headersCustomizer Customizer for HTTP headers security settings.
   * @param corsSpecCustomizer Customizer for CORS security settings.
   * @param apiAuthorizationCustomizer Customizer for API authorization security settings.
   * @param oauth2SpecCustomizer Customizer for OAuth2 resource server security settings.
   * @param csrfSpecCustomizer Customizer for CSRF security settings.
   * @return The configured SecurityWebFilterChain.
   */
  @Bean
  SecurityWebFilterChain springSecurityFilterChain(
      ServerHttpSecurity http,
      HeadersCustomizer headersCustomizer,
      CorsCustomizer corsSpecCustomizer,
      ApiAuthorizationCustomizer apiAuthorizationCustomizer,
      Oauth2Customizer oauth2SpecCustomizer,
      CsrfCustomizer csrfSpecCustomizer
  ) {
    http
        .headers(headersCustomizer)
        .authorizeExchange(apiAuthorizationCustomizer)
        .oauth2ResourceServer(oauth2SpecCustomizer)
        .cors(corsSpecCustomizer)
        .csrf(csrfSpecCustomizer)
        .httpBasic(Customizer.withDefaults());

    return http.build();
  }

  /**
   * Adds security headers to all responses.
   */
  @Bean
  public WebFilter securityHeadersWebFilter() {
    return (exchange, chain) -> {
      exchange.getResponse()
          .getHeaders()
          .add("Cross-Origin-Opener-Policy", "same-origin");

      exchange.getResponse()
          .getHeaders()
          .add("Cross-Origin-Embedder-Policy", "require-corp");

      exchange.getResponse()
          .getHeaders()
          .add("Cross-Origin-Resource-Policy", "same-origin");

      exchange.getResponse()
          .getHeaders()
          .add(
              "Cache-Control",
              "no-store, no-cache, must-revalidate, max-age=0"
          );

      exchange.getResponse()
          .getHeaders()
          .add("Pragma", "no-cache");

      return chain.filter(exchange);
    };
  }

  /**
   * Creates a ReactiveJwtDecoder bean used to decode JWTs in a reactive context.
   *
   * @param configuration contains the JWKS URL.
   * @return configured ReactiveJwtDecoder.
   */
  @Bean
  public ReactiveJwtDecoder jwtDecoder(ForestClientConfiguration configuration) {
    return NimbusReactiveJwtDecoder
        .withJwkSetUri(configuration.getSecurity().getJwksUrl())
        .build();
  }

  /**
   * Creates a MapReactiveUserDetailsService bean used to load reactive user details.
   *
   * @param configuration contains service account definitions.
   * @return configured MapReactiveUserDetailsService.
   */
  @Bean
  public MapReactiveUserDetailsService userDetailsService(
      ForestClientConfiguration configuration
  ) {
    return new MapReactiveUserDetailsService(
        configuration.getSecurity().getServiceAccounts()
            .stream()
            .map(serviceAccount -> new User(
                serviceAccount.name(),
                String.format("{noop}%s", serviceAccount.secret()),
                List.of(new SimpleGrantedAuthority(
                    "ROLE_" + ApplicationConstant.USERTYPE_SERVICE_USER
                ))
            ))
            .map(UserDetails.class::cast)
            .toList()
    );
  }
  
}