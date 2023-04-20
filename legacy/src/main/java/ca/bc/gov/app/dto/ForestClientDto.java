package ca.bc.gov.app.dto;

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
}
