package ca.bc.gov.app.dto.client;

import java.util.List;

public record ClientAddressDto(
    String streetAddress,
    String country,
    String province,
    String city,
    String postalCode,
    int index,
    List<ClientContactDto> contacts
) {
}
