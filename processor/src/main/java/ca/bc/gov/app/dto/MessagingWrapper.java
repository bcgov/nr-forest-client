package ca.bc.gov.app.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

  @SuppressWarnings({"unchecked", "unused"})
  public <P> P getParameter(String key,Class<P> clazz) {
    if(Objects.isNull(this.parameters.get(key)))
      return null;
    return (P) this.parameters.get(key);
  }

}
