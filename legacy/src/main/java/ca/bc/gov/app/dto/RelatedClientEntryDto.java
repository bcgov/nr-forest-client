package ca.bc.gov.app.dto;

import lombok.With;

@With
public record RelatedClientEntryDto(
    RelatedClientDto client,
    RelatedClientDto relatedClient,
    CodeNameDto relationship,
    Float percentageOwnership,
    Boolean hasSigningAuthority,
    boolean isMainParticipant
) {

}
