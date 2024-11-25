package ca.bc.gov.app.dto.legacy;

import java.time.LocalDate;
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
  LocalDate clientCommentUpdateDate,
  String clientCommentUpdateUser,
  String goodStandingInd,
  LocalDate birthdate,
  
  List<ForestClientLocationDto> addresses,
  List<ForestClientContactDto> contacts
) {
  
}
