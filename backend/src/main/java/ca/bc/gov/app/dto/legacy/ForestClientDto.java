package ca.bc.gov.app.dto.legacy;

import org.apache.commons.lang3.StringUtils;

public record ForestClientDto(
    String clientNumber,
    String clientName,
    String legalFirstName,
    String legalMiddleName,
    String clientStatusCode,
    String clientTypeCode,
    String clientIdTypeCode,
    String clientIdentification,
    String registryCompanyTypeCode,
    String corpRegnNmbr,
    String clientAcronym,
    String wcbFirmNumber,
    String ocgSupplierNmbr,
    String clientComment
) {

  public String legalName() {
    if (StringUtils.defaultString(clientTypeCode).equalsIgnoreCase("I")) {
      return
          StringUtils.defaultString(legalFirstName) +
              StringUtils.defaultString(legalFirstName) +
              StringUtils.defaultString(legalMiddleName) +
              StringUtils.defaultString(clientName);
    }
    return StringUtils.defaultString(clientName);
  }
}