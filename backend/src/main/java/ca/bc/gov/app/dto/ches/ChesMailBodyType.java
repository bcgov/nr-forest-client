package ca.bc.gov.app.dto.ches;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public enum ChesMailBodyType {

  HTML("html"),
  TEXT("text");

  @Getter
  private final String value;
  private static final Map<String, ChesMailBodyType>
      CONSTANTS = new HashMap<>();

  static {
    for (ChesMailBodyType c : values()) {
      CONSTANTS.put(c.value, c);
    }
  }

  ChesMailBodyType(String value) {
    this.value = value;
  }

  @JsonValue
  public String value() {
    return this.value;
  }

  /**
   * Returns the enum constant with the specified string value.
   *
   * @param value the string value representing the enum constant
   * @return the enum constant with the specified string value
   * @throws IllegalArgumentException if no enum constant with the specified string value exists
   */
  @JsonCreator
  public static ChesMailBodyType fromValue(String value) {
    ChesMailBodyType constant = CONSTANTS.get(value);
    if (constant == null) {
      throw new IllegalArgumentException(value);
    } else {
      return constant;
    }
  }

}
