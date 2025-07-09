package ca.bc.gov.app.dto.legacy;

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
) {}
