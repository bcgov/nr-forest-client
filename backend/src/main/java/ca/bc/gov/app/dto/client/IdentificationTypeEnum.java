package ca.bc.gov.app.dto.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Map;

/**
 * Enumeration representing different types of identification used by clients.
 * <p>
 * This enum defines various types of identification documents, such as birth certificates (BRTH),
 * Canadian driver's licenses (CDDL), passports (PASS), citizenship documents (CITZ), First Nations
 * ID (FNID), US driver's licenses (USDL), and other forms of identification (OTHR). It includes a
 * static block to initialize a map for reverse lookup, allowing retrieval of enum instances by
 * their name.
 * </p>
 */
public enum IdentificationTypeEnum {
  // Enum constants representing identification types
  BRTH, // Birth Certificate
  CDDL, // Canadian Driver's License
  PASS, // Passport
  CITZ, // Citizenship Document
  FNID, // First Nations ID
  USDL, // US Driver's License
  OTHR; // Other forms of identification

  // A map for reverse lookup of enum constants by their name
  private static final Map<String, IdentificationTypeEnum> CONSTANTS = new java.util.HashMap<>();

  static {
    // Populates the CONSTANTS map with enum names and their corresponding enum instances
    for (IdentificationTypeEnum c : values()) {
      CONSTANTS.put(c.name(), c);
    }
  }

  /**
   * Retrieves the enum instance corresponding to the given string value.
   * <p>
   * This method allows for reverse lookup of enum instances by their name, facilitating the
   * conversion from strings to enum instances. It supports dynamic retrieval of enum instances in
   * scenarios where the enum type is determined at runtime.
   * </p>
   *
   * @param value The string representation of the enum constant to be retrieved.
   * @return The {@link IdentificationTypeEnum} instance corresponding to the given string value, or
   * null if no matching instance is found.
   */
  @JsonCreator
  public static IdentificationTypeEnum fromValue(String value) {
    return CONSTANTS.get(value);
  }
}
