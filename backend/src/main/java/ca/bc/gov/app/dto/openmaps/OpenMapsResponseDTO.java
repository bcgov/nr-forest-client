package ca.bc.gov.app.dto.openmaps;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OpenMapsResponseDTO(
    Integer numberReturned,
    @JsonProperty("features")
    List<FeatureDTO> features
) {
  public boolean empty() {
    return numberReturned == null || numberReturned.equals(0);
  }
}
