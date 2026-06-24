package ca.bc.gov.app.converters;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

@DisplayName("Unit Test | Granted Authorities Converter")
class GrantedAuthoritiesConverterTest {

  private final GrantedAuthoritiesConverter sut = new GrantedAuthoritiesConverter();

  @Test
  @DisplayName("convert should uppercase and prefix roles from cognito groups")
  void shouldLoadRolesFromCognitoGroups() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("custom:idp_name", "idir");
    claims.put("cognito:groups", List.of("client_admin", "data_entry"));

    assertAuthorities(
        claims,
        List.of("ROLE_CLIENT_ADMIN", "ROLE_DATA_ENTRY", "ROLE_IDIR_USER")
    );
  }

  @Test
  @DisplayName("convert should ignore null entries in cognito groups")
  void shouldIgnoreNullEntriesInCognitoGroups() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("custom:idp_name", "idir");
    claims.put("cognito:groups", Arrays.asList("client_admin", null, "data_entry"));

    assertAuthorities(
        claims,
        List.of("ROLE_CLIENT_ADMIN", "ROLE_DATA_ENTRY", "ROLE_IDIR_USER")
    );
  }

  @Test
  @DisplayName("convert should ignore empty or missing cognito groups")
  void shouldIgnoreEmptyOrMissingCognitoGroups() {
    assertAuthorities(
        Map.of(
            "custom:idp_name", "idir",
            "cognito:groups", List.of()
        ),
        List.of("ROLE_IDIR_USER")
    );

    assertAuthorities(
        Map.of(
            "custom:idp_name", "idir",
            "other-claim", "value"
        ),
        List.of("ROLE_IDIR_USER")
    );
  }

  @Test
  @DisplayName("convert should ignore null or non-list cognito groups")
  void shouldIgnoreNullOrNonListCognitoGroups() {
    Map<String, Object> nullGroupsClaims = new HashMap<>();
    nullGroupsClaims.put("custom:idp_name", "idir");
    nullGroupsClaims.put("cognito:groups", null);

    assertAuthorities(nullGroupsClaims, List.of("ROLE_IDIR_USER"));

    assertAuthorities(
        Map.of(
            "custom:idp_name", "idir",
            "cognito:groups", "client_admin"
        ),
        List.of("ROLE_IDIR_USER")
    );
  }

  private void assertAuthorities(Map<String, Object> claims, List<String> expectedAuthorities) {
    List<String> actualAuthorities = sut.convert(createJwt(claims))
        .stream()
        .map(GrantedAuthority::getAuthority)
        .toList();

    assertIterableEquals(expectedAuthorities, actualAuthorities);
  }

  private Jwt createJwt(Map<String, Object> claims) {
    return new Jwt(
        "token-value",
        Instant.now(),
        Instant.now().plusSeconds(3600),
        Map.of("alg", "HS256", "typ", "JWT"),
        claims
    );
  }
}
