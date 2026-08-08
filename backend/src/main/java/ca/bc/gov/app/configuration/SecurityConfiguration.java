package ca.bc.gov.app.configuration;

import ca.bc.gov.app.ApplicationConstant;
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
   * <p>Since Spring Security 7, the {@code Customizer} beans ({@code HeadersCustomizer},
   * {@code CorsCustomizer}, {@code ApiAuthorizationCustomizer}, {@code Oauth2Customizer} and
   * {@code CsrfCustomizer}) are automatically applied to the {@link ServerHttpSecurity} instance
   * when it is created, so they no longer need to be wired up explicitly here.
   *
   * @param http The ServerHttpSecurity instance that is used to build the security filter chain.
   *
   * @return The configured SecurityWebFilterChain.
   */
  @Bean
  SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    http.httpBasic(Customizer.withDefaults());
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
