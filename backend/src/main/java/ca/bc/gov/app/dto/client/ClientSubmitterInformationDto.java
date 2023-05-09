package ca.bc.gov.app.dto.client;

import java.util.Map;

public record ClientSubmitterInformationDto(
    String userId,
    String submitterFirstName,
    String submitterLastName,
    String submitterPhoneNumber,
    String submitterEmail
) {
  public Map<String, Object> description() {
    return Map.of(
        "id", userId,
        "firstName", submitterFirstName,
        "lastName", submitterLastName,
        "name", String.join(" ", submitterFirstName, submitterLastName),
        "phone", submitterPhoneNumber,
        "email", submitterEmail
    );
  }
}
