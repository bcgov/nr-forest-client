package ca.bc.gov.app.converters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Converts a {@link Jwt} into Spring Security authorities.
 */
@Slf4j
public class GrantedAuthoritiesConverter
    implements Converter<Jwt, Collection<GrantedAuthority>> {

  /**
   * Converts the token claims into granted authorities.
   *
   * @param jwt the token to convert
   * @return the extracted authorities
   */
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    return Stream.concat(loadRoles(jwt).stream(), loadRoleFromIdp(jwt).stream()).toList();
  }

  /**
   * Loads authorities from the {@code cognito:groups} claim.
   *
   * @param jwt the token to inspect
   * @return the extracted authorities
   */
  private List<GrantedAuthority> loadRoles(Jwt jwt) {

    Object rawRoles = jwt.getClaims().getOrDefault("cognito:groups", List.of());

    List<String> roles = new ArrayList<>();

    if (rawRoles instanceof List<?> rawRolesList) {
      roles = rawRolesList.stream()
          .filter(Objects::nonNull)
          .map(Object::toString)
          .map(role -> role.toUpperCase(Locale.ROOT))
          .toList();
    }

    return roles.stream()
        .map(role -> "ROLE_" + role)
        .map(SimpleGrantedAuthority::new)
        .map(GrantedAuthority.class::cast)
        .toList();
  }

  /**
   * Loads an authority from the {@code custom:idp_name} claim.
   *
   * @param jwt the token to inspect
   * @return a single authority derived from the claim, or a fallback {@code ROLE_NONE_USER}
   *     authority when the claim is missing
   */
  private List<GrantedAuthority> loadRoleFromIdp(Jwt jwt) {
    return Optional.ofNullable(jwt.getClaims().getOrDefault("custom:idp_name", "none"))
        .map(String::valueOf)
        .map(idp -> idp.startsWith("ca.bc.gov.flnr.fam.") ? "bcsc" : idp)
        .map(idp -> idp.toUpperCase(Locale.ROOT))
        .map(idp -> "ROLE_" + idp + "_USER")
        .map(SimpleGrantedAuthority::new)
        .map(GrantedAuthority.class::cast)
        .map(List::of)
        .orElse(List.of());
  }
}