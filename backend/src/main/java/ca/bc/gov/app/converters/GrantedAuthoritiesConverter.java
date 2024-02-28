package ca.bc.gov.app.converters;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

@Slf4j
public class GrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

  public Collection<GrantedAuthority> convert(Jwt jwt) {
    return
        Optional
            // Reads the claim
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