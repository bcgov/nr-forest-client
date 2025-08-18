package ca.bc.gov.app.util;

import static ca.bc.gov.app.ApplicationConstant.FIRST_NAME;
import static ca.bc.gov.app.ApplicationConstant.LAST_NAME;

import ca.bc.gov.app.ApplicationConstant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * This is a utility class for handling JWT principals. It provides methods to extract various
 * attributes from a JwtAuthenticationToken object. The class is designed with a private constructor
 * to prevent instantiation.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class JwtPrincipalUtil {

  /**
   * Retrieves the provider of the JWT token from the given JwtAuthenticationToken principal. The
   * provider is extracted from the token attributes under the key "custom:idp_name". If the
   * provider starts with "ca.bc.gov.flnr.fam.", it is replaced with "BCSC". If the provider is not
   * blank, it is returned in uppercase. If the provider is blank, an empty string is returned.
   *
   * @param principal JwtAuthenticationToken object from which the provider is to be extracted.
   * @return The provider of the JWT token in uppercase, or an empty string if the provider is
   * blank.
   */
  public static String getProvider(JwtAuthenticationToken principal) {
    return getProviderValue(principal.getTokenAttributes());
  }

  /**
   * Retrieves the provider of the JWT token from the given Jwt principal. The provider is extracted
   * from the token attributes under the key "custom:idp_name". If the provider starts with
   * "ca.bc.gov.flnr.fam.", it is replaced with "BCSC". If the provider is not blank, it is returned
   * in uppercase. If the provider is blank, an empty string is returned.
   *
   * @param principal Jwt object from which the provider is to be extracted.
   * @return The provider of the JWT token in uppercase, or an empty string if the provider is
   * blank.
   */
  public static String getProvider(Jwt principal) {
    return getProviderValue(principal.getClaims());
  }

  /**
   * Retrieves the user ID from the given JwtAuthenticationToken principal. The user ID is extracted
   * from the token attributes under the key "custom:idp_username". If the user ID is blank, the
   * value under the key "custom:idp_user_id" is used. If both values are blank, an empty string is
   * returned. If the user ID is not blank, it is prefixed with the provider in uppercase and a
   * backslash.
   *
   * @param principal JwtAuthenticationToken object from which the user ID is to be extracted.
   * @return The user ID prefixed with the provider in uppercase and a backslash, or an empty string
   * if the user ID is blank.
   */
  public static String getUserId(JwtAuthenticationToken principal) {
    return getUserIdValue(principal.getTokenAttributes());
  }

  /**
   * Retrieves the user ID from the given Jwt principal. The user ID is extracted from the token
   * attributes under the key "custom:idp_username". If the user ID is blank, the value under the
   * key "custom:idp_user_id" is used. If both values are blank, an empty string is returned. If the
   * user ID is not blank, it is prefixed with the provider in uppercase and a backslash.
   *
   * @param principal Jwt object from which the user ID is to be extracted.
   * @return The user ID prefixed with the provider in uppercase and a backslash, or an empty string
   * if the user ID is blank.
   */
  public static String getUserId(Jwt principal) {
    return getUserIdValue(principal.getClaims());
  }

  /**
   * Retrieves the business ID from the given JwtAuthenticationToken principal. The business ID is
   * extracted from the token attributes under the key "custom:idp_business_id". If the business ID
   * is blank, an empty string is returned.
   *
   * @param principal JwtAuthenticationToken object from which the business ID is to be extracted.
   * @return The business ID, or an empty string if the business ID is blank.
   */
  public static String getBusinessId(JwtAuthenticationToken principal) {
    return getBusinessIdValue(principal.getTokenAttributes());
  }

  /**
   * Retrieves the business ID from the given Jwt principal. The business ID is extracted from the
   * token attributes under the key "custom:idp_business_id". If the business ID is blank, an empty
   * string is returned.
   *
   * @param principal Jwt object from which the business ID is to be extracted.
   * @return The business ID, or an empty string if the business ID is blank.
   */
  public static String getBusinessId(Jwt principal) {
    return getBusinessIdValue(principal.getClaims());
  }

  /**
   * Retrieves the business name from the given JwtAuthenticationToken principal. The business name
   * is extracted from the token attributes under the key "custom:idp_business_name". If the
   * business name is blank, an empty string is returned.
   *
   * @param principal JwtAuthenticationToken object from which the business name is to be
   *                  extracted.
   * @return The business name, or an empty string if the business name is blank.
   */
  public static String getBusinessName(JwtAuthenticationToken principal) {
    return getBusinessNameValue(principal.getTokenAttributes());
  }

  /**
   * Retrieves the business name from the given Jwt principal. The business name is extracted from
   * the token attributes under the key "custom:idp_business_name". If the business name is blank,
   * an empty string is returned.
   *
   * @param principal Jwt object from which the business name is to be extracted.
   * @return The business name, or an empty string if the business name is blank.
   */
  public static String getBusinessName(Jwt principal) {
    return getBusinessNameValue(principal.getClaims());
  }

  /**
   * Retrieves the business name from the given JwtAuthenticationToken principal. The business name
   * is extracted from the token attributes under the key "custom:idp_business_name". If the
   * business name is blank, an empty string is returned.
   *
   * @param principal JwtAuthenticationToken object from which the business name is to be
   *                  extracted.
   * @return The business name, or an empty string if the business name is blank.
   */
  public static String getEmail(JwtAuthenticationToken principal) {
    return getEmailValue(principal.getTokenAttributes());
  }

  /**
   * Retrieves the business name from the given Jwt principal. The business name is extracted from
   * the token attributes under the key "custom:idp_business_name". If the business name is blank,
   * an empty string is returned.
   *
   * @param principal Jwt object from which the business name is to be extracted.
   * @return The business name, or an empty string if the business name is blank.
   */
  public static String getEmail(Jwt principal) {
    return getEmailValue(principal.getClaims());
  }

  /**
   * Retrieves the display name from the given JwtAuthenticationToken principal. The display name is
   * extracted from the token attributes under the key "custom:idp_display_name". If the display
   * name is blank, the first and last names are extracted and concatenated. If both the first and
   * last names are blank, an empty string is returned.
   *
   * @param principal JwtAuthenticationToken object from which the display name is to be extracted.
   * @return The display name, or the concatenated first and last names, or an empty string if both
   * the display name and the first and last names are blank.
   */
  public static String getName(JwtAuthenticationToken principal) {
    return getNameValue(principal.getTokenAttributes());
  }

  /**
   * Retrieves the display name from the given Jwt principal. The display name is extracted from the
   * token attributes under the key "custom:idp_display_name". If the display name is blank, the
   * first and last names are extracted and concatenated. If both the first and last names are
   * blank, an empty string is returned.
   *
   * @param principal Jwt object from which the display name is to be extracted.
   * @return The display name, or the concatenated first and last names, or an empty string if both
   * the display name and the first and last names are blank.
   */
  public static String getName(Jwt principal) {
    return getNameValue(principal.getClaims());
  }

  /**
   * Retrieves the last name from the given JwtAuthenticationToken principal. The last name is
   * extracted from the token attributes under the key "family_name". If the last name is blank, an
   * empty string is returned.
   *
   * @param principal JwtAuthenticationToken object from which the last name is to be extracted.
   * @return The last name or an empty string if the last name is blank.
   */
  public static String getLastName(JwtAuthenticationToken principal) {
    return getLastNameValue(principal.getTokenAttributes());
  }

  /**
   * Retrieves the last name from the given Jwt principal. The last name is extracted from the token
   * attributes under the key "family_name". If the last name is blank, an empty string is
   * returned.
   *
   * @param principal Jwt object from which the last name is to be extracted.
   * @return The last name or an empty string if the last name is blank.
   */
  public static String getLastName(Jwt principal) {
    return getLastNameValue(principal.getClaims());
  }

  /**
   * Retrieves a list of groups from the given JwtPrincipal. This method extracts the token
   * attributes from the provided {@link JwtAuthenticationToken}, then looks for the key
   * "cognito:groups" in the token attributes. If the value associated with this key is a
   * {@link List}, the method filters the elements to only include non-null values of type
   * {@link String}. The resulting list of strings is returned.
   *
   * @param jwtPrincipal The {@link JwtAuthenticationToken} containing the token attributes. It must
   *                     have the "cognito:groups" key. If the key does not exist or the value is
   *                     not a list of strings, an empty list is returned.
   * @return A list of group names, or an empty list if the key is missing or the value is not a
   * list of strings.
   */
  public static Set<String> getGroups(JwtAuthenticationToken jwtPrincipal) {
    if (jwtPrincipal == null || jwtPrincipal.getTokenAttributes() == null) {
      return Collections.emptySet();
    }
    return getClaimGroups(jwtPrincipal.getTokenAttributes());
  }

  /**
   * Retrieves a list of groups from the given JwtPrincipal. This method extracts the token
   * attributes from the provided {@link Jwt}, then looks for the key "cognito:groups" in the token
   * attributes. If the value associated with this key is a {@link List}, the method filters the
   * elements to only include non-null values of type {@link String}. The resulting list of strings
   * is returned.
   *
   * @param jwtPrincipal The {@link Jwt} containing the token attributes. It must have the
   *                     "cognito:groups" key. If the key does not exist or the value is not a list
   *                     of strings, an empty list is returned.
   * @return A list of group names, or an empty list if the key is missing or the value is not a
   * list of strings.
   */
  public static Set<String> getGroups(Jwt jwtPrincipal) {
    if (jwtPrincipal == null || jwtPrincipal.getClaims() == null) {
      return Collections.emptySet();
    }
    return getClaimGroups(jwtPrincipal.getClaims());
  }

  /**
   * Converts a comma-separated string of roles into a set of roles.
   *
   * @return A set of roles.
   */
  public static Set<String> getRoles() {
    String roleCsv = MDC.get(ApplicationConstant.MDC_USERROLES);
    if (StringUtils.isNotBlank(roleCsv)) {
      return Set.of(roleCsv.split(","));
    }
    return Set.of();
  }

  private static Set<String> getClaimGroups(Map<String, Object> tokenAttributes) {
    Object groups = tokenAttributes.get("cognito:groups");

    if (groups instanceof List) {
      return ((List<?>) groups).stream()
          .filter(Objects::nonNull)
          .filter(String.class::isInstance)
          .map(String.class::cast)
          .collect(Collectors.toSet());
    }

    return Collections.emptySet();
  }

  /**
   * Retrieves the value of a specified claim from the claims map. If the claim is not present,
   * returns an empty string.
   *
   * @param claims    The map containing the JWT claims.
   * @param claimName The name of the claim to retrieve.
   * @return The value of the specified claim as a String, or an empty string if the claim is not
   * present.
   */
  private static String getClaimValue(Map<String, Object> claims, String claimName) {
    return claims
        .getOrDefault(claimName, StringUtils.EMPTY)
        .toString();
  }

  /**
   * Retrieves the provider value from the JWT claims. The provider is identified by the key
   * "custom:idp_name" within the claims. If the provider's name starts with "ca.bc.gov.flnr.fam.",
   * it is replaced with "BCSC". Otherwise, the provider's name is returned in uppercase. If the
   * provider is not specified, an empty string is returned.
   *
   * @param claims The map containing the JWT claims.
   * @return The provider's name in uppercase or "BCSC" if it starts with "ca.bc.gov.flnr.fam.", or
   * an empty string if the provider is not specified.
   */
  private static String getProviderValue(Map<String, Object> claims) {
    String provider = getClaimValue(claims, "custom:idp_name");

    if (StringUtils.isNotBlank(provider)) {
      return provider.startsWith("ca.bc.gov.flnr.fam.") ? "BCSC" : provider.toUpperCase(Locale.ROOT);
    }
    return StringUtils.EMPTY;
  }

  /**
   * Retrieves the business name value from the JWT claims. The business name is identified by the
   * key "custom:idp_business_name" within the claims. If the business name is not specified, an
   * empty string is returned.
   *
   * @param claims The map containing the JWT claims.
   * @return The business name or an empty string if the business name is not specified.
   */
  private static String getBusinessNameValue(Map<String, Object> claims) {
    return getClaimValue(claims, "custom:idp_business_name");
  }

  /**
   * Constructs the user ID by combining the provider's name with the user's username or user ID.
   * The method first attempts to retrieve the user's username from the JWT claims using the key
   * "custom:idp_username". If the username is not present or is blank, it then attempts to retrieve
   * the user's ID using the key "custom:idp_user_id". If either value is found, it is combined with
   * the provider's name, separated by a backslash. If neither value is found, an empty string is
   * returned. This method ensures that the user ID is uniquely identified by prefixing it with the
   * provider's name.
   *
   * @param claims The map containing the JWT claims.
   * @return The constructed user ID in the format "Provider\Username" or "Provider\UserID", or an
   * empty string if neither the username nor the user ID is present in the claims.
   */
  private static String getUserIdValue(Map<String, Object> claims) {
    return
        Stream
            .of(
                getClaimValue(claims, "custom:idp_username"),
                getClaimValue(claims, "custom:idp_user_id")
            )
            .map(Object::toString)
            .filter(StringUtils::isNotBlank)
            .map(userId -> getProviderValue(claims) + "\\" + userId)
            .findFirst()
            .orElse(StringUtils.EMPTY);
  }

  /**
   * Retrieves the business ID from the JWT claims. The business ID is identified by the key
   * "custom:idp_business_id" within the claims. If the business ID is not specified or is blank, an
   * empty string is returned. This method is used to extract the business ID for further processing
   * or validation.
   *
   * @param claims The map containing the JWT claims.
   * @return The business ID as a string, or an empty string if the business ID is not specified.
   */
  private static String getBusinessIdValue(Map<String, Object> claims) {
    return
        getClaimValue(claims, "custom:idp_business_id");
  }

  /**
   * Retrieves the email value from the JWT claims. This method looks for the "email" key within the
   * claims map. If the email is specified, its value is returned. Otherwise, an empty string is
   * returned, indicating that the email claim is not present.
   *
   * @param claims The map containing the JWT claims.
   * @return The email value as a String, or an empty string if the "email" claim is not present.
   */
  private static String getEmailValue(Map<String, Object> claims) {
    return getClaimValue(claims, "email");
  }

  /**
   * Retrieves the display name value from the JWT claims. This method searches for the
   * "custom:idp_display_name" key within the claims map. If the display name is specified, its
   * value is returned. Otherwise, an empty string is returned, indicating that the display name
   * claim is not present.
   *
   * @param claims The map containing the JWT claims.
   * @return The display name value as a String, or an empty string if the "custom:idp_display_name"
   * claim is not present.
   */
  private static String getDisplayNameValue(Map<String, Object> claims) {
    return getClaimValue(claims, "custom:idp_display_name");
  }

  /**
   * Processes the given JWT claims to extract and assemble user name information. This method
   * extracts the business name, first name, and last name from the JWT claims. If the provider is
   * "bceidbusiness" or if both first and last names are missing, it attempts to parse the display
   * name to extract these names. The method then assembles and returns a map containing the
   * business name, first name, last name, and full name (a concatenation of first and last names).
   *
   * @param claims The map containing the JWT claims from which the name information is to be
   *               extracted.
   * @return A map with keys "businessName", "firstName", "lastName", and "fullName", containing the
   * extracted and/or computed name information. If specific name components are not found, their
   * values in the map will be empty strings.
   */
  private static Map<String, String> processName(Map<String, Object> claims) {
    Map<String, String> additionalInfo = new HashMap<>();

    // Extract business name if exists
    additionalInfo.put("businessName", getBusinessNameValue(claims));

    // Extract first and last names if they exist
    String firstName = getClaimValue(claims, "given_name");
    String lastName = getClaimValue(claims, "family_name");

    // Determine if special handling for names is required
    boolean useDisplayName =
        "bceidbusiness".equals(getProviderValue(claims)) || (firstName.isEmpty()
                                                             && lastName.isEmpty());
    if (useDisplayName) {
      Map<String, String> names = ClientMapper.parseName(getDisplayNameValue(claims),
          getProviderValue(claims));
      firstName = names.get(FIRST_NAME);
      lastName = names.get(LAST_NAME);
    }

    // Put extracted or computed first and last names into the map
    additionalInfo.put(FIRST_NAME, firstName.trim());
    additionalInfo.put(LAST_NAME, lastName.trim());
    additionalInfo.put("fullName", String.join(" ", firstName, lastName).trim());

    return additionalInfo;
  }

  /**
   * Retrieves the last name value from the JWT claims. This method utilizes the {@code processName}
   * method to extract and assemble user name information from the JWT claims, specifically
   * targeting the last name. If the last name is not specified, an empty string is returned.
   *
   * @param claims The map containing the JWT claims.
   * @return The last name extracted from the JWT claims, or an empty string if not specified.
   */
  private static String getLastNameValue(Map<String, Object> claims) {
    return processName(claims).get(LAST_NAME);
  }

  /**
   * Retrieves the full name value from the JWT claims. This method leverages the
   * {@code processName} method to extract and assemble user name information from the JWT claims,
   * focusing on assembling the full name (a concatenation of first and last names). If both first
   * and last names are not specified, an empty string is returned.
   *
   * @param claims The map containing the JWT claims.
   * @return The full name (concatenation of first and last names) extracted from the JWT claims, or
   * an empty string if not specified.
   */
  private static String getNameValue(Map<String, Object> claims) {
    return processName(claims).get("fullName");
  }


}
