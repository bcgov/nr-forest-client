package ca.bc.gov.app.dto.legacy;

import java.time.LocalDate;
import java.util.List;
import lombok.With;

/**
 * A Data Transfer Object (DTO) representing the details of a forest client.
 *
 * @param clientNumber The unique number identifying the client.
 * @param clientName The name of the client.
 * @param legalFirstName The legal first name of the client.
 * @param legalMiddleName The legal middle name of the client.
 * @param clientStatusCode The status code of the client.
 * @param clientStatusDesc The description of the client's status.
 * @param clientTypeCode The type code of the client.
 * @param clientTypeDesc The description of the client's type.
 * @param clientIdTypeCode The identification type code of the client.
 * @param clientIdTypeDesc The description of the client's identification type.
 * @param clientIdentification The identification of the client.
 * @param registryCompanyTypeCode The registry company type code.
 * @param corpRegnNmbr The corporate registration number.
 * @param clientAcronym The acronym of the client.
 * @param wcbFirmNumber The WCB (Workers' Compensation Board) firm number.
 * @param clientComment Any comments about the client.
 * @param clientCommentUpdateDate The date when the client comment was last updated.
 * @param clientCommentUpdateUser The user who last updated the client comment.
 * @param goodStandingInd Indicator of whether the client is in good standing.
 * @param birthdate The birthdate of the client.
 * @param addresses The list of addresses associated with the client.
 * @param contacts The list of contacts associated with the client.
 * @param doingBusinessAs The list of "doing business as" names associated with the client.
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
    LocalDate clientCommentUpdateDate,
    String clientCommentUpdateUser,
    String goodStandingInd,
    LocalDate birthdate,

    List<ForestClientLocationDto> addresses,
    List<ForestClientContactDto> contacts,
    List<ForestClientDoingBusinessAsDto> doingBusinessAs
) {

}