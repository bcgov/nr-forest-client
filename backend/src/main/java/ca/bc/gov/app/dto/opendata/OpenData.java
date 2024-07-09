package ca.bc.gov.app.dto.opendata;

import java.util.List;

public record OpenData(
    String type,
    Crs crs,
    List<Feature> features
) {
}
