package ca.bc.gov.app.dto.client;

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
    int index,

    String locationName
) {

  public Map<String, Object> description() {

    final String indexFormatted = String.format("address.[%d]", index);

    return
        Map.of(indexFormatted,
            Map.of(
                "name", locationName,
                "address", streetAddress,
                "complementaryAddressOne", complementaryAddressOne,
                "complementaryAddressTwo", complementaryAddressTwo,
                "country", country.text(),
                "province", province.text(),
                "city", city,
                "postalCode", postalCode
            )
        );

  }
}
