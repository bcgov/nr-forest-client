package ca.bc.gov.app.dto.legacy;

import lombok.With;

@With
public record ForestClientDoingBusinessAsDto(
    String clientNumber,
    String doingBusinessAsName,
    String createdBy,
    String updatedBy,
    Long orgUnit
) {

}
