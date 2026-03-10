package ca.bc.gov.app.dto.bcregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.With;

@With
@JsonIgnoreProperties(ignoreUnknown = true)
public record BcRegistryPartyDto(
    BcRegistryOfficerDto officer,
    List<BcRegistryRoleDto> roles
) {

  public boolean isProprietor() {
    if (roles == null) {
      return false;
    }
    return roles
        .stream()
        .filter(BcRegistryRoleDto::active)
        .anyMatch(role -> "Proprietor".equalsIgnoreCase(role.roleType()));
  }
  
}