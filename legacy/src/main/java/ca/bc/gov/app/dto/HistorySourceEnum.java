package ca.bc.gov.app.dto;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public enum HistorySourceEnum {
  
  CLIENT_INFORMATION("cli"), 
  LOCATION("loc"), 
  CONTACT("ctc"), 
  DBA("dba");

  @Getter
  private final String value;
  private static final Map<String, HistorySourceEnum> SOURCES = new HashMap<>();

  static {
    for (HistorySourceEnum source : values()) {
      SOURCES.put(source.value, source);
    }
  }

  HistorySourceEnum(String value) {
    this.value = value;
  }

  public static HistorySourceEnum fromValue(String value) {
    HistorySourceEnum source = SOURCES.get(value.toLowerCase());
    if (source == null) {
      throw new IllegalArgumentException(value);
    }
    return source;
  }
}
