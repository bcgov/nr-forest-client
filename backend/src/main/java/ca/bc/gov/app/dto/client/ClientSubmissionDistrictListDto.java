package ca.bc.gov.app.dto.client;

public record ClientSubmissionDistrictListDto (
    long id,
    String district,
    String emails
) {
}