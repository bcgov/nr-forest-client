package ca.bc.gov.app.dto;

import java.util.HashMap;
import java.util.Map;

public record MessagingWrapper<T>(
    T payload,
    Map<String, Object> parameters
) {

  public MessagingWrapper(T payload, Map<String, Object> parameters) {
    this.payload = payload;
    this.parameters = new HashMap<>(parameters);
  }


  public MessagingWrapper<T> withParameter(String key, Object value) {
    this.parameters.put(key, value);
    return this;
  }

}
