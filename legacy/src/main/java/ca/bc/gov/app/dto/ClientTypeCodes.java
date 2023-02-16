package ca.bc.gov.app.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClientTypeCodes {
  INDIVIDUAL("I"),
  ASSOCIATION("A"),
  CORPORATION("C"),
  FIRST_NATION_BAND("B"),
  UNREGISTERED_COMPANY("U");

  private final String value;

  ClientTypeCodes(String value) {
    this.value = value;
  }

  @JsonValue
  public String value() {
    return this.value;
  }

  @JsonCreator
  public static ClientTypeCodes fromValue(String value) {
    for (ClientTypeCodes c : values()) {
      if (c.value().equalsIgnoreCase(value)) {
        return c;
      }
    }
    throw new IllegalArgumentException(value);
  }

}
