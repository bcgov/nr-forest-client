package ca.bc.gov.app.dto.bcregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BcRegistryAddressDto(
    Integer id,
    String addressCity,
    String addressCountry,
    String addressRegion,
    String addressType,
    String deliveryInstructions,
    String postalCode,
    String streetAddress,
    String streetAddressAdditional
) {
}
