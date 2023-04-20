package ca.bc.gov.app.dto.bcregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Optional;
import java.util.Set;
import lombok.With;

@With
@JsonIgnoreProperties(ignoreUnknown = true)
public record BcRegistryOfficesDto(
    BcRegistryBusinessAdressesDto businessOffice
) {
  public Set<BcRegistryAddressDto> addresses() {
    return Optional
        .ofNullable(businessOffice)
        .filter(BcRegistryBusinessAdressesDto::isValid)
        .map(BcRegistryBusinessAdressesDto::addresses)
        .orElse(Set.of());
  }
  public boolean isValid(){
    return businessOffice != null && businessOffice.isValid();
  }
}
