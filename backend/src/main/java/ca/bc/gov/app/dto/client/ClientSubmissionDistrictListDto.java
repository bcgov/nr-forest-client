package ca.bc.gov.app.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClientSubmissionDistrictListDto(
    @JsonProperty("id") long id,
    @JsonProperty("district") String district,
    @JsonProperty("emails") String emails
) {}
