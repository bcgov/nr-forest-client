package ca.bc.gov.app.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AddressCompleteRetrieveDto(
    @JsonProperty("Language")
    String language,

    @JsonProperty("City")
    String city,

    @JsonProperty("Province")
    String province,

    @JsonProperty("ProvinceName")
    String provinceName,

    @JsonProperty("CountryName")
    String countryName,

    @JsonProperty("CountryIso2")
    String countryIso2,

    @JsonProperty("PostalCode")
    String postalCode,

    @JsonProperty("Line1")
    String line1,

    @JsonProperty("Line2")
    String line2,

    @JsonProperty("Line3")
    String line3,

    @JsonProperty("Line4")
    String line4,

    @JsonProperty("Line5")
    String line5,

    @JsonProperty("Error")
    String error,

    @JsonProperty("Description")
    String description,

    @JsonProperty("Cause")
    String cause,

    @JsonProperty("Resolution")
    String resolution) implements AddressError {
}
