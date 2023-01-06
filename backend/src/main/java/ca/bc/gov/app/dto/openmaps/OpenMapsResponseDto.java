package ca.bc.gov.app.dto.openmaps;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OpenMapsResponseDto(
    Integer numberReturned,
    @JsonProperty("features")
    List<FeatureDto> features
) {
  public boolean empty() {
    return numberReturned == null || numberReturned.equals(0);
  }
}
