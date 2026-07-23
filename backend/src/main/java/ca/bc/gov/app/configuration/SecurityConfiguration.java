package ca.bc.gov.app.configuration;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.security.ApiAuthorizationCustomizer;
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
 *
 * <p>{@code ApiAuthorizationCustomizer} must NOT be registered as a Spring bean (neither
 * {@code @Component} nor {@code @Bean}). If it were, {@code ServerHttpSecurityConfiguration.
 * applyTopLevelBeanCustomizers} would auto-discover it via {@code context.getBeanProvider}
 * and call {@code http.authorizeExchange(apiAuthorizationCustomizer)} during
 * {@link ServerHttpSecurity} bean creation, registering {@code anyExchange()} prematurely.
 * That would prevent {@code ReactiveOAuth2ResourceServerWebSecurityAutoConfiguration} and
 * other auto-configurations from adding their own matchers to the shared
 * {@code ServerHttpSecurity} instance. Instead, it is instantiated locally and applied
 * explicitly in {@link #springSecurityFilterChain(ServerHttpSecurity)}.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

  /**
   * This method is a Spring Bean that configures the Spring Security filter chain.
   */
  @Bean
  SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    http
        .authorizeExchange(new ApiAuthorizationCustomizer())
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
