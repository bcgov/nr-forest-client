package ca.bc.gov.app.dto.bcregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.With;

@With
@JsonIgnoreProperties(ignoreUnknown = true)
public record BcRegistryDocumentDto(
    List<BcRegistryPartyDto> parties
) {

  public BcRegistryPartyDto getProprietor() {
    if (parties == null) {
      return null;
    }
    return parties
        .stream()
        .filter(BcRegistryPartyDto::isProprietor)
        .findFirst()
        .orElse(null);
  }
}