package ca.bc.gov.app.dto;

public record SubmissionInformationDto(
    String legalName,
    String incorporationNumber,
    Boolean goodStanding
) {
}
