package ca.bc.gov.app.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
    String provider = principal
        .getTokenAttributes()
        .getOrDefault("custom:idp_name", StringUtils.EMPTY)
        .toString();

    if (StringUtils.isNotBlank(provider)) {
      return provider.startsWith("ca.bc.gov.flnr.fam.") ? "BCSC" : provider.toUpperCase();
    }
    return StringUtils.EMPTY;
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
    return
        Stream
            .of(
                principal.getTokenAttributes()
                    .getOrDefault("custom:idp_username",StringUtils.EMPTY),
                principal.getTokenAttributes()
                    .getOrDefault("custom:idp_user_id",StringUtils.EMPTY)
            )
            .map(Object::toString)
            .filter(StringUtils::isNotBlank)
            .map(userId -> getProvider(principal) + "\\" + userId)
            .findFirst()
            .orElse(StringUtils.EMPTY);
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
    return principal
        .getTokenAttributes()
        .getOrDefault("custom:idp_business_id", StringUtils.EMPTY)
        .toString();
  }

  /**
   * Retrieves the business name from the given JwtAuthenticationToken principal. The business name
   * is extracted from the token attributes under the key "custom:idp_business_name". If the
   * business name is blank, an empty string is returned.
   *
   * @param principal JwtAuthenticationToken object from which the business name is to be extracted.
   * @return The business name, or an empty string if the business name is blank.
   */
  public static String getEmail(JwtAuthenticationToken principal) {
    return principal
        .getTokenAttributes()
        .getOrDefault("email", StringUtils.EMPTY)
        .toString();
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
    Map<String, String> names = processName(principal);
    return String.join(" ", names.get("firstName"), names.get("lastName"));
  }

  /**
   * Retrieves the first name from the given JwtAuthenticationToken principal. The first name is
   * extracted from the token attributes under the key "given_name". If the first name is blank, the
   * display name is extracted and split. If the display name is blank, an empty string is returned.
   *
   * @param principal JwtAuthenticationToken object from which the first name is to be extracted.
   * @return The first name, or an empty string if the first name is blank.
   */
  private static Map<String, String> processName(JwtAuthenticationToken principal) {
    Map<String, Object> payload = principal.getTokenAttributes();
    Map<String, String> additionalInfo = new HashMap<>();

    // Extract business name if exists
    additionalInfo.put("businessName",
        String.valueOf(payload.getOrDefault("custom:idp_business_name", StringUtils.EMPTY)));

    // Extract first and last names if they exist
    String firstName = String.valueOf(payload.getOrDefault("given_name", StringUtils.EMPTY));
    String lastName = String.valueOf(payload.getOrDefault("family_name", StringUtils.EMPTY));

    // Determine if special handling for names is required
    boolean useDisplayName = "bceidbusiness" .equals(getProvider(principal)) || (firstName.isEmpty()
                                                                                 && lastName.isEmpty());
    if (useDisplayName) {
      String displayName = String.valueOf(payload.get("custom:idp_display_name"));
      String[] nameParts =
          displayName.contains(",") ? displayName.split(",") : displayName.split(" ");

      if ("IDIR" .equals(getProvider(principal)) && nameParts.length >= 2) {
        // For IDIR, split by comma and then by space for the first name as the value will be Lastname, Firsname MIN:XX
        lastName = nameParts[0].trim();
        firstName = nameParts[1].split(" ")[0].trim();
      } else if (nameParts.length >= 2) {
        // For others, assume space separates the first and last names
        firstName = nameParts[0].trim();
        lastName = String.join(" ", Arrays.copyOfRange(nameParts, 1, nameParts.length));
      }
    }

    // Put extracted or computed first and last names into the map
    additionalInfo.put("firstName", firstName);
    additionalInfo.put("lastName", lastName);

    return additionalInfo;
  }

}
