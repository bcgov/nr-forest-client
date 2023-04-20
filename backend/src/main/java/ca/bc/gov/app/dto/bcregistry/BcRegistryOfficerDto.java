package ca.bc.gov.app.dto.bcregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BcRegistryOfficerDto(
    String email,
    String firstName,
    String lastName,
    String middleInitial,
    String partyType
) {
}
