package ca.bc.gov.app.dto.client;

import java.util.List;

public record ClientAddressDto(
    String streetAddress,
    ClientValueTextDto country,
    ClientValueTextDto province,
    String city,
    String postalCode,
    List<ClientContactDto> contacts
) {
}
