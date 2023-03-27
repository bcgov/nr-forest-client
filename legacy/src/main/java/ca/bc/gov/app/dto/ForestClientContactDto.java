package ca.bc.gov.app.dto;

import lombok.With;

@With
public record ForestClientContactDto(
    Long id,
    String clientNumber,
    String locationCode,
    String contactCode,
    String name,
    String businessPhone,
    String cellPhone,
    String faxNumber,
    String emailAddress
) {
}
