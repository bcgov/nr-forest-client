package ca.bc.gov.app.dto.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines the types of clients using an enumeration.
 * <p>
 * This enumeration represents different types of clients identified by single or multiple
 * characters (e.g., C, S, A, I, P, L, RSP, USP). It includes a static block to initialize a map for
 * reverse lookup, allowing retrieval of enum instances by their name.
 * </p>
 */
public enum ClientTypeEnum {
  // Enum constants representing client types
  C, S, A, I, P, L, RSP, USP;

  // A map for reverse lookup of enum constants by their name
  private static final Map<String, ClientTypeEnum> CONSTANTS = new HashMap<>();

  static {
    // Populates the CONSTANTS map with enum names and their corresponding enum instances
    for (ClientTypeEnum c : values()) {
      CONSTANTS.put(c.name(), c);
    }
  }

  /**
   * Retrieves the enum instance corresponding to the given string value.
   * <p>
   * This method allows for reverse lookup of enum instances by their name, facilitating the
   * conversion from strings to enum instances.
   * </p>
   *
   * @param value The string representation of the enum constant to be retrieved.
   * @return The {@link ClientTypeEnum} instance corresponding to the given string value, or null if
   * no matching instance is found.
   */
  @JsonCreator
  public static ClientTypeEnum fromValue(String value) {
    return CONSTANTS.get(value);
  }
}
