package ca.bc.gov.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@DisplayName("Unit Test | JwtPrincipalUtil")
class JwtPrincipalUtilTest {

  @Test
  @DisplayName("get provider returns provider in uppercase when provider is not blank")
  void getProvider_returnsProviderInUppercase_whenProviderIsNotBlank() {
    JwtAuthenticationToken principal = createJwtAuthenticationTokenWithAttributes(
        Map.of("custom:idp_name",
            "ca.bc.gov.flnr.fam.dev"));
    String expected = "BCSC";
    String actual = JwtPrincipalUtil.getProvider(principal);
    assertEquals(expected, actual);
  }

  @Test
  @DisplayName("get provider returns provider in uppercase when provider is not blank")
  void getProvider_returnsEmptyString_whenProviderIsBlank() {
    JwtAuthenticationToken principal = createJwtAuthenticationTokenWithAttributes(
        Map.of("custom:idp_name",
            ""));
    String expected = "";
    String actual = JwtPrincipalUtil.getProvider(principal);
    assertEquals(expected, actual);
  }

  @Test
  @DisplayName("get userId returns userId prefixed with provider when userId is not blank")
  void getUserId_returnsUserIdPrefixedWithProvider_whenUserIdIsNotBlank() {
    JwtAuthenticationToken principal = createJwtAuthenticationTokenWithAttributes(
        Map.of("custom:idp_username", "username","custom:idp_name",
            "ca.bc.gov.flnr.fam.dev")
    );
    String expected = "BCSC\\username";
    String actual = JwtPrincipalUtil.getUserId(principal);
    assertEquals(expected, actual);
  }

  @Test
  @DisplayName("get userId returns userId prefixed with provider when userId is not blank")
  void getUserId_returnsEmptyString_whenUserIdIsBlank() {
    JwtAuthenticationToken principal = createJwtAuthenticationTokenWithAttributes(
        Map.of("custom:idp_username", ""));
    String expected = "";
    String actual = JwtPrincipalUtil.getUserId(principal);
    assertEquals(expected, actual);
  }

  @Test
  @DisplayName("get userId returns userId prefixed with provider when userId is not blank")
  void getBusinessId_returnsBusinessId_whenBusinessIdIsNotBlank() {
    JwtAuthenticationToken principal = createJwtAuthenticationTokenWithAttributes(
        Map.of("custom:idp_business_id", "businessId"));
    String expected = "businessId";
    String actual = JwtPrincipalUtil.getBusinessId(principal);
    assertEquals(expected, actual);
  }

  @Test
  @DisplayName("get userId returns userId prefixed with provider when userId is not blank")
  void getBusinessId_returnsEmptyString_whenBusinessIdIsBlank() {
    JwtAuthenticationToken principal = createJwtAuthenticationTokenWithAttributes(
        Map.of("custom:idp_business_id", ""));
    String expected = "";
    String actual = JwtPrincipalUtil.getBusinessId(principal);
    assertEquals(expected, actual);
  }

  private JwtAuthenticationToken createJwtAuthenticationTokenWithAttributes(
      Map<String, Object> attributes
  ) {
    return new JwtAuthenticationToken(
        new Jwt(
            "token",
            LocalDateTime.now().minusMinutes(10).toInstant(ZoneOffset.UTC),
            LocalDateTime.now().plusMinutes(90).toInstant(ZoneOffset.UTC),
            Map.of("alg", "HS256", "typ", "JWT"),
            attributes
        ),
        List.of()
    );
  }
}