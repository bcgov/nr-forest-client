package ca.bc.gov.app.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.bc.gov.app.util.JwtPrincipalUtil;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@DisplayName("Unit Test | JwtPrincipalUtil")
class JwtPrincipalUtilTest {

  @ParameterizedTest(name = "For custom:idp_name {0} provider is {1}")
  @CsvSource({
      "ca.bc.gov.flnr.fam.dev, BCSC",
      "idir, IDIR",
      "bceidbusiness, BCEIDBUSINESS",
      "'', ''"
  })
  @DisplayName("get provider")
  void shouldGetProvider(String idpName, String provider) {
    Map<String, Object> claims = Map.of("custom:idp_name", idpName);

    assertEquals(provider,
        JwtPrincipalUtil.getProvider(createJwtAuthenticationTokenWithAttributes(claims)));
    assertEquals(provider,
        JwtPrincipalUtil.getProvider(createJwt(claims)));
  }

  @ParameterizedTest(name = "For custom:idp_username {0} and custom:idp_name {1} userId is {2}")
  @CsvSource({
      "username, userid, ca.bc.gov.flnr.fam.dev, BCSC\\username",
      "username, userid, idir, IDIR\\username",
      "username, userid, bceidbusiness, BCEIDBUSINESS\\username",
      "'', userid, ca.bc.gov.flnr.fam.dev, BCSC\\userid",
      "'', userid, idir, IDIR\\userid",
      "'', userid, bceidbusiness, BCEIDBUSINESS\\userid",
      "'', '','', ''"
  })
  @DisplayName("get userId returns userId prefixed with provider when userId is not blank")
  void shouldGetUserId(
      String idpUsername,
      String idpUserId,
      String idpName,
      String expected
  ) {
    Map<String, Object> claims = Map.of(
        "custom:idp_username", idpUsername,
        "custom:idp_user_id", idpUserId,
        "custom:idp_name", idpName
    );

    assertEquals(expected,
        JwtPrincipalUtil.getUserId(createJwtAuthenticationTokenWithAttributes(claims)));
    assertEquals(expected, JwtPrincipalUtil.getUserId(createJwt(claims)));
  }

  @ParameterizedTest(name = "For custom:idp_business_id {0} id is {1}")
  @ValueSource(strings = {"businessId", ""})
  @DisplayName("get businessId")
  void shouldGetBusinessId(String value) {
    Map<String, Object> claims = Map.of("custom:idp_business_id", value);

    assertEquals(value,
        JwtPrincipalUtil.getBusinessId(createJwtAuthenticationTokenWithAttributes(claims)));
    assertEquals(value, JwtPrincipalUtil.getBusinessId(createJwt(claims)));
  }

  @ParameterizedTest(name = "For custom:idp_business_name {0} name is {1}")
  @ValueSource(strings = {"The Business Name", ""})
  @DisplayName("get businessName")
  void shouldGetBusinessName(String value) {
    Map<String, Object> claims = Map.of("custom:idp_business_name", value);

    assertEquals(value,
        JwtPrincipalUtil.getBusinessName(createJwtAuthenticationTokenWithAttributes(claims)));
    assertEquals(value, JwtPrincipalUtil.getBusinessName(createJwt(claims)));
  }

  @ParameterizedTest(name = "For email {0} email is {1}")
  @ValueSource(strings = {"my_email_is@mail.ca", ""})
  @DisplayName("get email")
  void shouldGetEmail(String value) {
    Map<String, Object> claims = Map.of("email", value);

    assertEquals(value,
        JwtPrincipalUtil.getEmail(createJwtAuthenticationTokenWithAttributes(claims)));
    assertEquals(value, JwtPrincipalUtil.getEmail(createJwt(claims)));
  }

  @ParameterizedTest(name = "For given_name {0} family_name {1} custom:idp_display_name {2} custom:idp_name {3} fullname is {4}")
  @CsvSource({
      "John, Wick, '',  ca.bc.gov.flnr.fam.dev, John Wick",
      "John, Wick, '',  idir, John Wick",
      "'', '', 'John Wick',  bceidbusiness, John Wick",
      "'', '', 'John Valeus Wick',  bceidbusiness, John Valeus Wick",
      "'', '', 'Wick, John WLRS:EX',  idir, John Wick",
      "'', '', 'da Silva, Anderson WLRS:EX',  idir, Anderson da Silva",
      "'', '', 'Wick, John V WLRS:EX',  idir, John V Wick",
      "'', '', '',  bceidbusiness, ''",
      "'', '', '', '', ''"
  })
  @DisplayName("get name")
  void shouldGetName(
      String givenName,
      String familyName,
      String displayName,
      String idpName,
      String expected
  ) {
    Map<String, Object> claims = Map.of(
        "given_name", givenName,
        "family_name", familyName,
        "custom:idp_name", idpName,
        "custom:idp_display_name", displayName
    );

    assertEquals(expected,
        JwtPrincipalUtil.getName(createJwtAuthenticationTokenWithAttributes(claims)));
    assertEquals(expected, JwtPrincipalUtil.getName(createJwt(claims)));
  }

  @ParameterizedTest(name = "For given_name {0} family_name {1} custom:idp_display_name {2} custom:idp_name {3} fullname is {4}")
  @CsvSource({
      "John, Wick, '',  ca.bc.gov.flnr.fam.dev, Wick",
      "John, Wick, '',  idir, Wick",
      "'', '', 'John Wick',  bceidbusiness, Wick",
      "'', '', 'John Valeus Wick',  bceidbusiness, Valeus Wick",
      "'', '', 'Wick, John WLRS:EX',  idir, Wick",
      "'', '', 'da Silva, Anderson WLRS:EX',  idir, da Silva",
      "'', '', 'Wick, John V WLRS:EX',  idir, V Wick",
      "'', '', '',  bceidbusiness, ''",
      "'', '', '', '', ''"
  })
  @DisplayName("get last name")
  void shouldGetLastName(
      String givenName,
      String familyName,
      String displayName,
      String idpName,
      String expected
  ) {
    Map<String, Object> claims = Map.of(
        "given_name", givenName,
        "family_name", familyName,
        "custom:idp_name", idpName,
        "custom:idp_display_name", displayName
    );

    assertEquals(expected,
        JwtPrincipalUtil.getLastName(createJwtAuthenticationTokenWithAttributes(claims)));
    assertEquals(expected, JwtPrincipalUtil.getLastName(createJwt(claims)));
  }

  private JwtAuthenticationToken createJwtAuthenticationTokenWithAttributes(
      Map<String, Object> attributes
  ) {
    return new JwtAuthenticationToken(
        createJwt(attributes),
        List.of()
    );
  }

  private static @NotNull Jwt createJwt(Map<String, Object> attributes) {
    return new Jwt(
        "token",
        LocalDateTime.now().minusMinutes(10).toInstant(ZoneOffset.UTC),
        LocalDateTime.now().plusMinutes(90).toInstant(ZoneOffset.UTC),
        Map.of("alg", "HS256", "typ", "JWT"),
        attributes
    );
  }
  
  @ParameterizedTest
  @DisplayName("getGroups should return expected group list")
  @MethodSource("provideGroupsTestData")
  void shouldGetGroups(Map<String, Object> tokenAttributes, List<String> expectedGroups) {
      JwtAuthenticationToken jwtAuthenticationToken = tokenAttributes == null
          ? null
          : createJwtAuthenticationTokenWithAttributes(tokenAttributes);

      List<String> actualGroups = JwtPrincipalUtil.getGroups(jwtAuthenticationToken);

      assertEquals(expectedGroups, actualGroups);
  }

  private static Stream<Arguments> provideGroupsTestData() {
      return Stream.of(
          // Case 1: Token attributes contain "CLIENT_ADMIN"
          Arguments.of(
              Map.of("cognito:groups", List.of("CLIENT_ADMIN")),
              List.of("CLIENT_ADMIN")
          ),
          // Case 2: Token attributes contain an empty group list
          Arguments.of(
              Map.of("cognito:groups", List.of()),
              List.of()
          ),
          // Case 3: Token attributes contain null groups
          Arguments.of(
              new HashMap<>() {{
                  put("cognito:groups", null);
              }},
              List.of()
          ),
          // Case 4: Token attributes missing "cognito:groups"
          Arguments.of(
              Map.of("otherKey", "someValue"),
              List.of()
          ),
          // Case 5: Null JwtAuthenticationToken
          Arguments.of(
              null,
              List.of()
          )
      );
  }
  
}