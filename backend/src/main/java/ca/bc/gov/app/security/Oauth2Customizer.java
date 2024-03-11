package ca.bc.gov.app.security;

import ca.bc.gov.app.converters.GrantedAuthoritiesConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity.OAuth2ResourceServerSpec;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * This class customizes the OAuth2 resource server specification for the application. It implements
 * the Customizer interface and overrides the customize method to set the JWT authentication
 * converter. The JWT authentication converter is set to a ReactiveJwtAuthenticationConverterAdapter
 * with a JwtAuthenticationConverter. The JwtAuthenticationConverter has a
 * GrantedAuthoritiesConverter set as the JWT granted authorities converter. This configuration
 * allows the extraction of granted authorities from the JWT.
 */
@Component
public class Oauth2Customizer implements Customizer<OAuth2ResourceServerSpec> {

  /**
   * This method customizes the OAuth2ResourceServerSpec by setting the JWT authentication
   * converter. The JWT authentication converter is set to the result of the
   * grantedAuthoritiesExtractor method.
   *
   * @param oAuth2ResourceServerSpec The OAuth2ResourceServerSpec to be customized.
   */
  @Override
  public void customize(OAuth2ResourceServerSpec oAuth2ResourceServerSpec) {
    oAuth2ResourceServerSpec.jwt(jwt ->
        jwt
            .jwtAuthenticationConverter(
                grantedAuthoritiesExtractor()
            )
    );
  }

  /**
   * This method creates a Converter that converts a Jwt to a Mono of AbstractAuthenticationToken.
   * It creates a JwtAuthenticationConverter and sets its JwtGrantedAuthoritiesConverter to a new
   * GrantedAuthoritiesConverter. It then returns a ReactiveJwtAuthenticationConverterAdapter that
   * wraps the JwtAuthenticationConverter.
   *
   * @return A Converter that converts a Jwt to a Mono of AbstractAuthenticationToken.
   */
  private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new GrantedAuthoritiesConverter());
    return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
  }
}
