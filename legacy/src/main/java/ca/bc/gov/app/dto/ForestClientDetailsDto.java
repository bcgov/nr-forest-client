package ca.bc.gov.app.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.With;

@With
public record ForestClientDetailsDto(
    String clientNumber,
    String clientName,
    String legalFirstName,
    String legalMiddleName,
    String clientStatusCode,
    String clientStatusDesc,
    String clientTypeCode,
    String clientTypeDesc,
    String clientIdTypeCode,
    String clientIdTypeDesc,
    String clientIdentification,
    String registryCompanyTypeCode,
    String corpRegnNmbr,
    String clientAcronym,
    String wcbFirmNumber,
    String clientComment,
    LocalDateTime clientCommentUpdateDate,
    String clientCommentUpdateUser,
    String goodStandingInd,
    LocalDateTime birthdate,

    List<ForestClientLocationDto> addresses,
    List<ForestClientContactDto> contacts,
    List<ClientDoingBusinessAsDto> doingBusinessAs
) {

}
