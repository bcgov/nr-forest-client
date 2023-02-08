package ca.bc.gov.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

public record ClientPublicViewDto(
    String clientNumber,
    String incorporationNumber,
    String clientName,
    String legalFirstName,
    String legalMiddleName,
    String clientStatusCode,
    String clientTypeCode,
    String clientNameInOrgBook

) {

  @JsonProperty
  public boolean sameName() {
    return StringUtils.equalsIgnoreCase(clientNameInOrgBook, clientName);
  }
}
