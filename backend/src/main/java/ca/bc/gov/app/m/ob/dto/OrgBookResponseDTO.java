package ca.bc.gov.app.m.ob.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrgBookResponseDTO(
    @JsonProperty("results")
    List<ResultDTO> results
){}