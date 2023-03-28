package ca.bc.gov.app.dto.client;

import lombok.With;

@With
public record ClientDetailsAddressDto(
    String streetAddress,
    ClientValueTextDto country,
    ClientValueTextDto province,
    String city,
    String postalCode,
    int index
) {
}
