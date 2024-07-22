package ca.bc.gov.app.dto.opendata;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Feature(
    String type,
    String id,
    Geometry geometry,
    @JsonProperty("geometry_name") String geometryName,
    FeatureProperties properties
) {
}
