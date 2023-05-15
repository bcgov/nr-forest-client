package ca.bc.gov.app.dto;

import lombok.With;

@With
public record SubmissionInformationDto(
    String legalName,
    String incorporationNumber,
    String goodStanding
) {
}
