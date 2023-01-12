package ca.bc.gov.app.dto;

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
  public boolean sameName() {
    return StringUtils.equalsIgnoreCase(clientNameInOrgBook, clientName);
  }
}
