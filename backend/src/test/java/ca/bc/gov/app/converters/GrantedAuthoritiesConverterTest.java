package ca.bc.gov.app.converters;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

@DisplayName("Unit Test | Granted Authorities Converter")
class GrantedAuthoritiesConverterTest {

  private final GrantedAuthoritiesConverter sut = new GrantedAuthoritiesConverter();

  @ParameterizedTest
  @MethodSource("convertRolesCases")
  @DisplayName("convert should load roles from cognito groups")
  void shouldLoadRolesFromCognitoGroups(
      Map<String, Object> claims,
      List<String> expectedAuthorities
  ) {
    List<String> actualAuthorities = sut.convert(createJwt(claims))
        .stream()
        .map(GrantedAuthority::getAuthority)
        .toList();

    assertIterableEquals(expectedAuthorities, actualAuthorities);
  }

  private static Stream<Arguments> convertRolesCases() {
    Map<String, Object> nullGroupsClaims = new HashMap<>();
    nullGroupsClaims.put("custom:idp_name", "idir");
    nullGroupsClaims.put("cognito:groups", null);

    return Stream.of(
        Arguments.of(
            Map.of(
                "custom:idp_name", "idir",
                "cognito:groups", List.of("client_admin", null, "data_entry")
            ),
            List.of("ROLE_CLIENT_ADMIN", "ROLE_DATA_ENTRY", "ROLE_IDIR_USER")
        ),
        Arguments.of(
            Map.of(
                "custom:idp_name", "idir",
                "cognito:groups", List.of()
            ),
            List.of("ROLE_IDIR_USER")
        ),
        Arguments.of(
            nullGroupsClaims,
            List.of("ROLE_IDIR_USER")
        ),
        Arguments.of(
            Map.of(
                "custom:idp_name", "idir",
                "other-claim", "value"
            ),
            List.of("ROLE_IDIR_USER")
        ),
        Arguments.of(
            Map.of(
                "custom:idp_name", "idir",
                "cognito:groups", "client_admin"
            ),
            List.of("ROLE_IDIR_USER")
        )
    );
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
