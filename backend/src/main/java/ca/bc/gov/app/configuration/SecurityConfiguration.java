package ca.bc.gov.app.configuration;

import ca.bc.gov.app.ApplicationConstant;
import ca.bc.gov.app.converters.GrantedAuthoritiesConverter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

  @Bean
  SecurityWebFilterChain springSecurityFilterChain(
      ServerHttpSecurity http,
      ForestClientConfiguration configuration
  ) {
    http
        .authorizeExchange(authorize ->
            authorize
                .pathMatchers("/metrics/**","/health/**").permitAll()

                .pathMatchers("/api/ches/email")
                .hasAnyRole(
                    ApplicationConstant.ROLE_SERVICE_USER
                )
                .pathMatchers("/api/ches/duplicate")
                .hasAnyRole(
                    ApplicationConstant.ROLE_BCEIDBUSINESS_USER,
                    ApplicationConstant.ROLE_BCSC_USER
                )
                .pathMatchers("/api/addresses/**")
                .hasAnyRole(
                    ApplicationConstant.ROLE_BCEIDBUSINESS_USER,
                    ApplicationConstant.ROLE_BCSC_USER
                )
                .pathMatchers("/api/codes/**")
                .hasAnyRole(
                    ApplicationConstant.ROLE_IDIR_USER,
                    ApplicationConstant.ROLE_BCEIDBUSINESS_USER,
                    ApplicationConstant.ROLE_BCSC_USER
                )
                .pathMatchers("/api/districts/**")
                .hasAnyRole(
                    ApplicationConstant.ROLE_IDIR_USER,
                    ApplicationConstant.ROLE_BCEIDBUSINESS_USER,
                    ApplicationConstant.ROLE_BCSC_USER,
                    ApplicationConstant.ROLE_SERVICE_USER
                )
                .pathMatchers("/api/countries/**")
                .hasAnyRole(
                    ApplicationConstant.ROLE_IDIR_USER,
                    ApplicationConstant.ROLE_BCEIDBUSINESS_USER,
                    ApplicationConstant.ROLE_BCSC_USER,
                    ApplicationConstant.ROLE_SERVICE_USER
                )
                // Only IDIR users can get details and approve/reject
                .pathMatchers("/api/clients/submissions/{id:[0-9]+}")
                .hasAnyRole(
                    ApplicationConstant.ROLE_IDIR_USER
                )
                // Only IDIR users can access the list, and other users can create
                .pathMatchers(HttpMethod.POST, "/api/clients/submissions/**")
                .hasAnyRole(
                    ApplicationConstant.ROLE_BCEIDBUSINESS_USER,
                    ApplicationConstant.ROLE_BCSC_USER
                )
                .pathMatchers(HttpMethod.GET, "/api/clients/submissions/**")
                .hasAnyRole(
                    ApplicationConstant.ROLE_IDIR_USER
                )

                // All BCSC and BCEID can access the client apis
                .pathMatchers("/api/clients/**")
                .hasAnyRole(
                    ApplicationConstant.ROLE_BCEIDBUSINESS_USER,
                    ApplicationConstant.ROLE_BCSC_USER
                )
                .anyExchange().authenticated()
        )
        .oauth2ResourceServer(resourceServer -> resourceServer.jwt(
            jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor())))
        .cors(
            corsSpec -> corsSpec.configurationSource(
                serverWebExchange -> {
                  var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                  corsConfig.setAllowedOrigins(
                      java.util.List.of(configuration.getFrontend().getUrl()));
                  corsConfig.setAllowedMethods(configuration.getFrontend().getCors().getMethods());
                  corsConfig.setAllowedHeaders(configuration.getFrontend().getCors().getHeaders());
                  corsConfig.setExposedHeaders(configuration.getFrontend().getCors().getHeaders());
                  corsConfig.setMaxAge(configuration.getFrontend().getCors().getAge().getSeconds());
                  corsConfig.setAllowCredentials(true);
                  return corsConfig;
                }
            )
        )
        .csrf(
            csrf -> csrf.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse()))
        .httpBasic(Customizer.withDefaults());
    return http.build();
  }

  @Bean
  public ReactiveJwtDecoder jwtDecoder(ForestClientConfiguration configuration) {
    return NimbusReactiveJwtDecoder
        .withJwkSetUri(configuration.getSecurity().getJwksUrl())
        .build();
  }

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
                    String.format("{noop}%s",serviceAccount.secret()),
                List.of(new SimpleGrantedAuthority("ROLE_" + ApplicationConstant.ROLE_SERVICE_USER))
                )
            )
            .map(UserDetails.class::cast)
            .toList()
    );
  }


  private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
    JwtAuthenticationConverter jwtAuthenticationConverter =
        new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter
        (new GrantedAuthoritiesConverter());
    return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
  }


}
