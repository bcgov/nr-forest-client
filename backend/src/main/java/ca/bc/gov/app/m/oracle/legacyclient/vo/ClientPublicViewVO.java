package ca.bc.gov.app.m.oracle.legacyclient.vo;

import org.apache.commons.lang3.StringUtils;

public record ClientPublicViewVO(
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