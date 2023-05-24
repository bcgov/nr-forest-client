package ca.bc.gov.app.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AddressCompleteFindDto(
    @JsonProperty("Id")
    String id,
    @JsonProperty("Text")
    String text,
    @JsonProperty("Description")
    String description,

    @JsonProperty("Next")
    String next,

    @JsonProperty("Error")
    String error,

    @JsonProperty("Cause")
    String cause,

    @JsonProperty("Resolution")
    String resolution) implements AddressError {
}
