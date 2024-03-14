package ca.bc.gov.app.converters;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
/**
 * This class is a converter that takes a Jwt token and converts it into a collection of GrantedAuthority objects.
 * It implements the Converter interface provided by Spring Framework.
 */
@Slf4j
public class GrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

  /**
   * This method takes a Jwt token and converts it into a collection of GrantedAuthority objects.
   * It first reads the "custom:idp_name" claim from the Jwt token.
   * If the claim starts with "ca.bc.gov.flnr.fam.", it is converted to "bcsc".
   * The claim is then converted to uppercase and prefixed with "ROLE_" and suffixed with "_USER".
   * A new SimpleGrantedAuthority object is created with this string and added to the collection.
   * If the claim is null or empty, an empty list is returned.
   *
   * @param jwt the Jwt token to be converted
   * @return a collection of GrantedAuthority objects
   */
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    return
        Optional
            // Reads the "custom:idp_name" claim from the Jwt token
            .ofNullable(jwt
                .getClaims()
                .getOrDefault("custom:idp_name", "none")
            )
            // Convert the claim to a string
            .map(String::valueOf)
            // If the claim starts with "ca.bc.gov.flnr.fam." then convert it to "bcsc"
            .map(idp -> idp.startsWith("ca.bc.gov.flnr.fam.") ? "bcsc" : idp)
            // Convert the string to uppercase
            .map(String::toUpperCase)
            // Add the "ROLE_" prefix and "_USER" suffix to the string
            .map(idp -> "ROLE_" + idp + "_USER")
            // Create a new SimpleGrantedAuthority object with the string
            .map(SimpleGrantedAuthority::new)
            // Cast it to a GrantedAuthority
            .map(GrantedAuthority.class::cast)
            // Return the result as a list
            .map(List::of)
            // If the claim is null or empty, return an empty list
            .orElse(List.of());
  }
}