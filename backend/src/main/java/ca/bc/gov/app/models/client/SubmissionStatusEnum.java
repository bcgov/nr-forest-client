package ca.bc.gov.app.models.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SubmissionStatusEnum {
  P("In Progress"),
  A("Approved"),
  R("Rejected"),
  D("Deleted"),
  S("Submitted");

  private final String description;
  private static final Map<String, SubmissionStatusEnum>
      CONSTANTS = new HashMap<>();

  static {
    for (SubmissionStatusEnum c : values()) {
      CONSTANTS.put(c.description, c);
    }
  }

  @JsonValue
  public String value() {
    return this.description;
  }

  /**
   * Returns the enum constant with the specified string value.
   *
   * @param value the string value representing the enum constant
   * @return the enum constant with the specified string value
   * @throws IllegalArgumentException if no enum constant with the specified string value exists
   */
  @JsonCreator
  public static SubmissionStatusEnum fromValue(String value) {
    SubmissionStatusEnum constant = CONSTANTS.get(value);
    if (constant == null) {
      throw new IllegalArgumentException(value);
    } else {
      return constant;
    }
  }
}
