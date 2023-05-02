package ca.bc.gov.app.dto.client;

public record ClientSubmissionDto(
    String userId,
    ClientBusinessInformationDto businessInformation,
    ClientLocationDto location,
    ClientSubmitterInformationDto submitterInformation
) {
}
