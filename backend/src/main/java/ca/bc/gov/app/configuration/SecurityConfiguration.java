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

/**
 * This class configures the security settings for the application. It uses the @Configuration
 * annotation to indicate that it is a configuration class. It uses the @EnableWebFluxSecurity
 * annotation to enable Spring Security's reactive features.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

  /**
   * This method is a Spring Bean that configures the Spring Security filter chain.
   * The filter chain is a mechanism that Spring Security uses to apply security features to HTTP requests.
   *
   * <p>The application-specific customizers are applied explicitly here instead of being exposed as
   * generic {@code Customizer<T>} beans. Spring Security 7 auto-applies generic customizer beans
   * reflectively while creating the {@link ServerHttpSecurity} bean; keeping this wiring explicit
   * makes startup deterministic and avoids native/AOT failures during ServerHttpSecurity creation.
   *
   * @param http The ServerHttpSecurity instance that is used to build the security filter chain.
   * @param headersCustomizer customizes security response headers.
   * @param corsCustomizer customizes CORS.
   * @param csrfCustomizer customizes CSRF handling.
   * @param oauth2Customizer customizes JWT resource-server authentication.
   * @param apiAuthorizationCustomizer customizes endpoint authorization rules.
   *
   * @return The configured SecurityWebFilterChain.
   */
  @Bean
  SecurityWebFilterChain springSecurityFilterChain(
      ServerHttpSecurity http,
      HeadersCustomizer headersCustomizer,
      CorsCustomizer corsCustomizer,
      CsrfCustomizer csrfCustomizer,
      Oauth2Customizer oauth2Customizer,
      ApiAuthorizationCustomizer apiAuthorizationCustomizer
  ) {
    http
        .headers(headersCustomizer::customize)
        .cors(corsCustomizer::customize)
        .csrf(csrfCustomizer::customize)
        .oauth2ResourceServer(oauth2Customizer::customize)
        .authorizeExchange(apiAuthorizationCustomizer::customize)
        .httpBasic(Customizer.withDefaults());

    return http.build();
  }

  /**
   * This method creates a ReactiveJwtDecoder bean. The ReactiveJwtDecoder is used to decode JWTs in
   * a reactive context. It is configured with the JWKS URL from the ForestClientConfiguration.
   *
   * @param configuration The configuration object that contains the JWKS URL.
   * @return The configured ReactiveJwtDecoder.
   */
  @Bean
  public ReactiveJwtDecoder jwtDecoder(ForestClientConfiguration configuration) {
    return NimbusReactiveJwtDecoder
        .withJwkSetUri(configuration.getSecurity().getJwksUrl())
        .build();
  }

  /**
   * This method creates a MapReactiveUserDetailsService bean. The MapReactiveUserDetailsService is
   * used to load user details in a reactive context. It is configured with the service accounts
   * from the ForestClientConfiguration.
   *
   * @param configuration The configuration object that contains the service accounts.
   * @return The configured MapReactiveUserDetailsService.
   */
  @Bean
  public MapReactiveUserDetailsService userDetailsService(ForestClientConfiguration configuration) {
    return new MapReactiveUserDetailsService(
        configuration
            .getSecurity()
            .getServiceAccounts()
            .stream()
            .map(serviceAccount ->
                new User(
                    serviceAccount.name(),
                    String.format("{noop}%s", serviceAccount.secret()),
                    List.of(
                        new SimpleGrantedAuthority("ROLE_" + ApplicationConstant.USERTYPE_SERVICE_USER))
                )
            )
            .map(UserDetails.class::cast)
            .toList()
    );
  }

}
