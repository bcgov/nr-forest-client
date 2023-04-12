package ca.bc.gov.app.dto.bcregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.With;

@With
@JsonIgnoreProperties(ignoreUnknown = true)
public record BcRegistryPartyDto(
    BcRegistryAddressDto deliveryAddress,
    BcRegistryAddressDto mailingAddress,
    BcRegistryOfficerDto officer,
    List<BcRegistryRoleDto> roles
) {
  public boolean isValid() {
    return officer != null && (mailingAddress != null || deliveryAddress != null);
  }
}
