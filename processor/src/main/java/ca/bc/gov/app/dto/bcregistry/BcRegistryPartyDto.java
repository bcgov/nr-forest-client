package ca.bc.gov.app.dto.bcregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.With;
import org.apache.commons.lang3.StringUtils;

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
        .anyMatch(role -> StringUtils.equalsIgnoreCase(role.roleType(), "Proprietor"));
  }
}
