package ca.bc.gov.app.dto.client;

import java.util.List;
import java.util.Map;
import lombok.With;

@With
public record ClientContactDto(
    ClientValueTextDto contactType,
    String firstName,
    String lastName,
    String phoneNumber,
    String email,
    int index,
    List<ClientValueTextDto> locationNames
) {
  public Map<String, Object> description() {
    final String indexFormatted = String.format("contact.[%d]", index);

    return
        Map.of(indexFormatted,
            Map.of(
                "firstName", firstName,
                "lastName", lastName,
                "name", String.join(" ", firstName, lastName),
                "phone", phoneNumber,
                "email", email
            )
        );
  }
}
