package ca.bc.gov.app.dto.client;

import java.util.List;
import java.util.Map;
import lombok.With;
import org.apache.commons.lang3.StringUtils;

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
    String secondaryPhoneNumber,
    String faxNumber,
    String email,
    int index,
    List<ClientValueTextDto> locationNames
) {

  public Map<String, Object> description() {
    final String indexFormatted = String.format("contact.[%d]", index);

    return
        Map.of(indexFormatted,
            Map.of(
                "firstName", StringUtils.defaultString(firstName),
                "lastName", StringUtils.defaultString(lastName),
                "name", String.join(" ", firstName, lastName),
                "phone", StringUtils.defaultString(phoneNumber),
                "secondaryPhoneNumber", StringUtils.defaultString(secondaryPhoneNumber),
                "faxNumber", StringUtils.defaultString(faxNumber),
                "email", StringUtils.defaultString(email)
            )
        );
  }

  public ClientContactDto withIndexed(int index) {
    if (this.index() == index) {
      return this;
    }
    return this.withIndex(index);
  }

  public boolean isValid() {
    return !StringUtils.isAnyBlank(
        firstName,
        lastName
    );
  }
}
