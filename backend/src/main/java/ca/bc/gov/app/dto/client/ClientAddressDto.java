package ca.bc.gov.app.dto.client;

import java.util.List;

public record ClientAddressDto(
    String streetAddress,
    String country,
    String province,
    String city,
    String postalCode,
    String businessPhone,
    int index,
    List<ClientContactDto> clientContactDtoList
) {
}
