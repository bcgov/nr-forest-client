package ca.bc.gov.app.dto.bcregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BcRegistryAddressDto(
    String addressCity,
    String addressCountry,
    String addressRegion,
    String deliveryInstructions,
    String postalCode,
    String streetAddress,
    String streetAddressAdditional
) {
}
