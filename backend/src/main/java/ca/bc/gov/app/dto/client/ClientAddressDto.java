package ca.bc.gov.app.dto.client;

import java.util.List;

public record ClientAddressDto(
    String streetAddress,
    ClientCountryDto country,
    ClientProvinceDto province,
    String city,
    String postalCode,
    List<ClientContactDto> contacts
) {
}
