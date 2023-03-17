package ca.bc.gov.app.dto.bcregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BcRegistryBusinessAdressesDto(
    BcRegistryAddressDto mailingAddress,
    BcRegistryAddressDto deliveryAddress
) {
}
