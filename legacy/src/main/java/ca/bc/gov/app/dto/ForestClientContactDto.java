package ca.bc.gov.app.dto;

public record ForestClientContactDto(
    String clientNumber,
    String clientLocnCode,
    String contactCode,
    String contactName,
    String businessPhone,
    String emailAddress,
    String createdBy,
    String updatedBy,
    Long orgUnit
) {

}
