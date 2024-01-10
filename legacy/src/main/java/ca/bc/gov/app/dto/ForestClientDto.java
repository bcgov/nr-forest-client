package ca.bc.gov.app.dto;

import java.time.LocalDate;

public record ForestClientDto(
    String clientNumber,
    String clientName,
    String legalFirstName,
    String legalMiddleName,
    String clientStatusCode,
    String clientTypeCode,
    LocalDate birthdate,
    String clientIdTypeCode,
    String clientIdentification,
    String registryCompanyTypeCode,
    String corpRegnNmbr,
    String clientComment,
    String createdBy,
    String updatedBy,
    Long orgUnit
) {

}
