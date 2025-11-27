package ca.bc.gov.app.dto.client;

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

  public String getId() {
    return String.format("%s%s%s%s%s",
        client.client().code(),
        client.location().code(),
        relationship.code(),
        relatedClient.client().code(),
        relatedClient.location().code()
        );
  }

}
