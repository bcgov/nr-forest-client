package ca.bc.gov.app.exception;

import lombok.Getter;

@Getter
public class MatchFoundException extends RuntimeException {

  private final String fieldName;
  private final String value;

  public MatchFoundException(String fieldName, String value) {
    super(String.format("Match found for field %s with value %s", fieldName, value));
    this.fieldName = fieldName;
    this.value = value;
  }
}
