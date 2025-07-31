package ca.bc.gov.app.dto;

import lombok.With;

@With
public record RelatedClientDto(
    CodeNameDto client,
    CodeNameDto location
) {

}
