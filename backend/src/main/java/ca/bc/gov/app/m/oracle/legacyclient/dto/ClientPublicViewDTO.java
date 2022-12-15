package ca.bc.gov.app.m.oracle.legacyclient.dto;

import org.apache.commons.lang3.StringUtils;

public record ClientPublicViewDTO(
    String clientNumber,
    String incorporationNumber,
    String clientName,
    String legalFirstName,
    String legalMiddleName,
    String clientStatusCode,
    String clientTypeCode,
    String clientNameInOrgBook
) {
  public boolean isSameName() {
    return
        StringUtils.isNotBlank(clientNameInOrgBook) &&
            StringUtils.isNotBlank(clientName) &&
            clientNameInOrgBook.equalsIgnoreCase(clientName);
  }
}