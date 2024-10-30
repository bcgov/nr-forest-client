package ca.bc.gov.app.dto.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration representing different legal entity types.
 * <p>
 * This enum defines various legal entity types identified by unique codes (e.g., A, B, BC, C,
 * etc.). It includes a static block to initialize a map for reverse lookup, allowing retrieval of
 * enum instances by their name. This feature facilitates the conversion from string values to their
 * corresponding enum instances, especially useful in parsing data from external sources or user
 * input.
 * </p>
 */
public enum LegalTypeEnum {
  // Enum constants representing legal entity types
  A, B, BC, C, CP, EPR, FOR, LIC, REG, S, XS, XCP, SP, GP, LP, XL, XP, LL;

  // A map for reverse lookup of enum constants by their name
  private static final Map<String, LegalTypeEnum> CONSTANTS = new HashMap<>();

  static {
    // Populates the CONSTANTS map with enum names and their corresponding enum instances
    for (LegalTypeEnum c : values()) {
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
   * @return The {@link LegalTypeEnum} instance corresponding to the given string value, or null if
   * no matching instance is found.
   */
  @JsonCreator
  public static LegalTypeEnum fromValue(String value) {
    return CONSTANTS.get(value);
  }
}
