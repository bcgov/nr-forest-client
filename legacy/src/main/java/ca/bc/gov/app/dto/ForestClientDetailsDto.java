package ca.bc.gov.app.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.With;

/**
 * DTO representing detailed information about a forest client, including personal information,
 * status, type, identification, comments, and related lists of addresses, contacts, and DBAs.
 */
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

    List<ForestClientLocationDetailsDto> addresses,
    List<ForestClientContactDto> contacts,
    List<ClientDoingBusinessAsDto> doingBusinessAs
) {

}
