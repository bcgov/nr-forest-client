package ca.bc.gov.app.dto.bcregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.With;

@With
@JsonIgnoreProperties(ignoreUnknown = true)
public record BcRegistryDocumentDto(
    BcRegistryBusinessDto business,
    BcRegistryOfficesDto offices,
    List<BcRegistryPartyDto> parties
) {

  public boolean isOwnedByPerson() {
    List<BcRegistryPartyDto> localParties = parties == null ? List.of() : parties;
    return localParties.stream().anyMatch(BcRegistryPartyDto::isPerson);
  }

}