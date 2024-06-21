package ca.bc.gov.app.dto.client;

import java.util.HashMap;
import java.util.Map;
import lombok.With;
import org.apache.commons.lang3.StringUtils;


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
    descMap.put("name", StringUtils.defaultString(locationName));
    descMap.put("address", StringUtils.defaultString(streetAddress));
    descMap.put("complementaryAddressOne", StringUtils.defaultString(complementaryAddressOne));
    descMap.put("complementaryAddressTwo", StringUtils.defaultString(complementaryAddressTwo));
    descMap.put("country", StringUtils.defaultString(country.text()));
    descMap.put("province", StringUtils.defaultString(province.text()));
    descMap.put("city", StringUtils.defaultString(city));
    descMap.put("postalCode", StringUtils.defaultString(postalCode));
    descMap.put("businessPhoneNumber", StringUtils.defaultString(businessPhoneNumber));
    descMap.put("secondaryPhoneNumber", StringUtils.defaultString(secondaryPhoneNumber));
    descMap.put("faxNumber", StringUtils.defaultString(faxNumber));
    descMap.put("emailAddress", StringUtils.defaultString(emailAddress));
    descMap.put("notes", StringUtils.defaultString(notes));

    return Map.of(indexFormatted, descMap);

  }
}
