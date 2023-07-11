package ca.bc.gov.app.dto.client;

import java.util.List;
import java.util.Map;
import lombok.With;

/**
 * The type Client contact dto.
 *
 * @param contactType   the contact type
 * @param firstName     the first name
 * @param lastName      the last name
 * @param phoneNumber   the phone number
 * @param email         the email
 * @param index         zero-based index of the contact
 * @param locationNames name of locations associated to this contact
 */
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
