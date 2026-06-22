package ca.bc.gov.app.dto.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Map;

/**
 * Identifies the supported client identification types.
 *
 * <p>The enum values map to the identification codes used by the application:
 * BRTH, CDDL, PASS, CITZ, FNID, PRCD, BCID, USDL, and OTHR.
 *
 * <p>Use {@link #fromValue(String)} to resolve an enum constant from its string name.
 */
public enum IdentificationTypeEnum {

  // Enum constants representing identification types
  BRTH, // Birth Certificate
  CDDL, // Canadian Driver's License
  PASS, // Passport
  CITZ, // Citizenship Document
  FNID, // First Nations ID
  PRCD, // Permanent Resident Card
  BCID, // British Columbia Identification Card
  USDL, // US Driver's License
  OTHR; // Other forms of identification

  // Reverse lookup by enum name
  private static final Map<String, IdentificationTypeEnum> CONSTANTS =
      new java.util.HashMap<>();

  static {
    for (IdentificationTypeEnum c : values()) {
      CONSTANTS.put(c.name(), c);
    }
  }

  /**
   * Returns the enum constant matching the supplied name.
   *
   * @param value the enum name
   * @return the matching {@link IdentificationTypeEnum}, or {@code null} if no match exists
   */
  @JsonCreator
  public static IdentificationTypeEnum fromValue(String value) {
    return CONSTANTS.get(value);
  }
}