package ca.bc.gov.app.entity;

import lombok.With;

@With
public record ClientRelatedProjection(
    String clientNumber,
    String clientName,
    String clientLocnCode,
    String clientLocnName,
    String relatedClntNmbr,
    String relatedClntName,
    String relatedClntLocn,
    String relatedClntLocnName,
    String relationshipCode,
    String relationshipName,
    String signingAuthInd,
    Float percentOwnership,
    Boolean primaryClient
) {
  public String id(){
    return String.format("%s%s%s%s%s",
        clientNumber,
        clientLocnCode,
        relationshipCode,
        relatedClntNmbr,
        relatedClntLocn
        );
  }
}
