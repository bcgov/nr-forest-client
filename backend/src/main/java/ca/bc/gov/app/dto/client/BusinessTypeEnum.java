package ca.bc.gov.app.dto.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration representing different types of business entities.
 * <p>
 * This enum defines two types of business entities, represented by single characters: R
 * (Registered) and U (Unregistered). It includes a static block to initialize a map for reverse
 * lookup, allowing retrieval of enum instances by their name.
 * </p>
 */
public enum BusinessTypeEnum {
  // Enum constants representing business types
  R, // Registered
  U; // Unregistered

  // A map for reverse lookup of enum constants by their name
  private static final Map<String, BusinessTypeEnum> CONSTANTS = new HashMap<>();

  static {
    // Populates the CONSTANTS map with enum names and their corresponding enum instances
    for (BusinessTypeEnum c : values()) {
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
   * @return The {@link BusinessTypeEnum} instance corresponding to the given string value, or null
   * if no matching instance is found.
   */
  @JsonCreator
  public static BusinessTypeEnum fromValue(String value) {
    return CONSTANTS.get(value);
  }
}
