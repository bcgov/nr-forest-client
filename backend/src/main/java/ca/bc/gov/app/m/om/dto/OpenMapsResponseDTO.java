package ca.bc.gov.app.m.om.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OpenMapsResponseDTO(
    @JsonProperty("features")
    List<FeatureDTO> features
) {
}
