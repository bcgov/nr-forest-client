package ca.bc.gov.app.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.With;

@With
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
    String clientAcronym,
    String wcbFirmNumber,
    String ocgSupplierNmbr,
    String clientComment,

    List<String> knowAs,
    List<ForestClientLocationDto> locations
) {
}
