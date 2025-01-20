package ca.bc.gov.app.dto;

import lombok.With;

@With
public record ClientDoingBusinessAsDto(
    Integer id,
    String clientNumber,
    String doingBusinessAsName,
    String createdBy,
    String updatedBy,
    Long orgUnit
) {

}
