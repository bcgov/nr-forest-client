package ca.bc.gov.app.dto.opendata;

import java.util.List;

public record Geometry(
    String type,
    List<Double> coordinates
) {
}
