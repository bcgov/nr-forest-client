package ca.bc.gov.app.dto.client;

import java.util.HashMap;
import java.util.Map;
import lombok.With;


@With
public record ClientAddressDto(
    String streetAddress,
    String complementaryAddressOne,
    String complementaryAddressTwo,
    ClientValueTextDto country,
    ClientValueTextDto province,
    String city,
    String postalCode,
    String businessPhoneNumber,
    String secondaryPhoneNumber,
    String faxNumber,
    String emailAddress,
    String notes,
    int index,

    String locationName
) {

  public Map<String, Object> description() {

    final String indexFormatted = String.format("address.[%d]", index);

    Map<String, Object> descMap = new HashMap<>();
    descMap.put("name", locationName);
    descMap.put("address", streetAddress);
    descMap.put("complementaryAddressOne", complementaryAddressOne);
    descMap.put("complementaryAddressTwo", complementaryAddressTwo);
    descMap.put("country", country.text());
    descMap.put("province", province.text());
    descMap.put("city", city);
    descMap.put("postalCode", postalCode);
    descMap.put("businessPhoneNumber", businessPhoneNumber);
    descMap.put("secondaryPhoneNumber", secondaryPhoneNumber);
    descMap.put("faxNumber", faxNumber);
    descMap.put("emailAddress", emailAddress);
    descMap.put("notes", notes);

    return Map.of(indexFormatted, descMap);

  }
}
