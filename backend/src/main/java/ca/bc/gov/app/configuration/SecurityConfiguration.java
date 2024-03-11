package ca.bc.gov.app.configuration;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.security.ForestHeadersCustomizer;
import ca.bc.gov.app.security.CorsCustomizer;
import ca.bc.gov.app.security.ApiAuthorizationCustomizer;
import ca.bc.gov.app.security.Oauth2Customizer;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
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
   * This method configures the SecurityWebFilterChain, which is the main security filter for the
   * application. It customizes the ServerHttpSecurity object by setting the authorization rules,
   * OAuth2 resource server settings, CORS settings, CSRF settings, and HTTP Basic settings. It then
   * builds the ServerHttpSecurity object into a SecurityWebFilterChain and returns it.
   *
   * @param http                 The ServerHttpSecurity object to be customized.
   * @param corsSpecCustomizer   The customizer for the CORS settings.
   * @param apiAuthorizationCustomizer   The customizer for the authorization rules.
   * @param oauth2SpecCustomizer The customizer for the OAuth2 resource server settings.
   * @param headersCustomizer    The customizer for the headers settings.
   * @return The configured SecurityWebFilterChain.
   */
  @Bean
  SecurityWebFilterChain springSecurityFilterChain(
      ServerHttpSecurity http,
      ForestHeadersCustomizer headersCustomizer,
      CorsCustomizer corsSpecCustomizer,
      ApiAuthorizationCustomizer apiAuthorizationCustomizer,
      Oauth2Customizer oauth2SpecCustomizer
  ) {
    http
        .headers(headersCustomizer)
        .authorizeExchange(apiAuthorizationCustomizer)
        .oauth2ResourceServer(oauth2SpecCustomizer)
        .cors(corsSpecCustomizer)
        .csrf(CsrfSpec::disable)
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
                        new SimpleGrantedAuthority("ROLE_" + ApplicationConstant.ROLE_SERVICE_USER))
                )
            )
            .map(UserDetails.class::cast)
            .toList()
    );
  }

}
