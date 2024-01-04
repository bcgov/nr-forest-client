package ca.bc.gov.app.dto.bcregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BcRegistryOfficerDto(
    String email,
    String firstName,
    String lastName,
    String middleInitial,
    String partyType
) {

  public boolean isPerson() {
    return StringUtils.isNotBlank(partyType) && partyType.equalsIgnoreCase("Person");
  }
}
