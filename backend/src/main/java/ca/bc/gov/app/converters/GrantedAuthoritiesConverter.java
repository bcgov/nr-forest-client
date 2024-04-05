package ca.bc.gov.app.converters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * This class is a converter that takes a Jwt token and converts it into a collection of
 * GrantedAuthority objects. It implements the Converter interface provided by Spring Framework.
 */
@Slf4j
public class GrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

/**
 * This method is responsible for converting a Jwt token into a collection of GrantedAuthority objects.
 * It does this by concatenating the streams of loadRoles(jwt) and loadRoleFromIdp(jwt) and collecting the results into a list.
 *
 * @param jwt the Jwt token to be converted
 * @return a collection of GrantedAuthority objects
 */
public Collection<GrantedAuthority> convert(Jwt jwt) {
  return Stream
      .concat(loadRoles(jwt).stream(), loadRoleFromIdp(jwt).stream())
      .toList();
}

/**
 * This method is responsible for loading roles from a Jwt token.
 * The results are collected into a list and returned.
 *
 * @param jwt the Jwt token from which to load roles
 * @return a list of GrantedAuthority objects
 */
private List<GrantedAuthority> loadRoles(Jwt jwt) {

  // It first retrieves the "cognito:groups" claim from the Jwt token, which is a list of roles.
  Object rawRoles = jwt
      .getClaims()
      .getOrDefault("cognito:groups", List.of());

  List<String> roles = new ArrayList<>();

  // It then converts each role to a string, converts each string to uppercase, and collects the results into a list.
  if (rawRoles instanceof List<?> rawRolesList) {
    roles = rawRolesList.stream()
        .filter(Objects::nonNull)
        .map(Object::toString)
        .map(String::toUpperCase)
        .toList();
  }

  // Then prefixes each string with "ROLE_", and converts each string into a SimpleGrantedAuthority object.
  return roles.stream()

      .map(role -> "ROLE_" + role)
      .map(SimpleGrantedAuthority::new)
      .map(GrantedAuthority.class::cast)
      .toList();
}

/**
 * This method is responsible for loading a role from a Jwt token based on the "custom:idp_name" claim.
 * If the claim starts with "ca.bc.gov.flnr.fam.", it is converted to "bcsc".
 * The claim is then converted to uppercase, prefixed with "ROLE_", suffixed with "_USER", and converted into a SimpleGrantedAuthority object.
 * The result is returned as a list.
 * If the claim is null or empty, an empty list is returned.
 *
 * @param jwt the Jwt token from which to load the role
 * @return a list containing a single GrantedAuthority object, or an empty list
 */
private List<GrantedAuthority> loadRoleFromIdp(Jwt jwt) {
  return Optional
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