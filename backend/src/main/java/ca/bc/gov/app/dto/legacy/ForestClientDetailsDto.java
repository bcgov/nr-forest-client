package ca.bc.gov.app.dto.legacy;

import java.util.List;

public record ForestClientDetailsDto (
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
  String clientComment,
  String goodStandingInd,
  List<ForestClientLocationDto> addresses,
  List<ForestClientContactDto> contacts
) {
  
}
