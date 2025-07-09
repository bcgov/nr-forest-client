package ca.bc.gov.app.dto.client;

import lombok.With;

@With
public record RelatedClientDto(
    CodeNameDto client,
    CodeNameDto location
) {

}
