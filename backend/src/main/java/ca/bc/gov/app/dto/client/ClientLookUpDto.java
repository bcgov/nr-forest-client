package ca.bc.gov.app.dto.client;

import lombok.With;


@With
public record ClientLookUpDto(
    String code,
    String name,
    String status,
    String legalType
) {

}
