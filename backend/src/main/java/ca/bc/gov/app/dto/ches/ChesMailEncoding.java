package ca.bc.gov.app.dto.ches;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public enum ChesMailEncoding {

  BASE_64("base64"),
  BINARY("binary"),
  HEX("hex"),
  UTF_8("utf-8");
  @Getter
  private final String value;
  private static final Map<String, ChesMailEncoding>
      CONSTANTS = new HashMap<>();

  static {
    for (ChesMailEncoding c : values()) {
      CONSTANTS.put(c.value, c);
    }
  }

  ChesMailEncoding(String value) {
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
  public static ChesMailEncoding fromValue(String value) {
    ChesMailEncoding constant = CONSTANTS.get(value);
    if (constant == null) {
      throw new IllegalArgumentException(value);
    } else {
      return constant;
    }
  }

}
