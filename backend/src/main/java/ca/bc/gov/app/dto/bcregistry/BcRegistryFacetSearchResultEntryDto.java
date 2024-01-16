package ca.bc.gov.app.dto.bcregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.With;

@With
@JsonIgnoreProperties(ignoreUnknown = true)
public record BcRegistryFacetSearchResultEntryDto(
    String bn,
    String identifier,
    String legalType,
    String name,
    String status,
    Boolean goodStanding
) {
}
