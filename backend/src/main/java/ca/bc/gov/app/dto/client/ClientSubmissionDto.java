package ca.bc.gov.app.dto.client;

public record ClientSubmissionDto(
    ClientBusinessInformationDto businessInformation,
    ClientLocationDto location,
    ClientSubmitterInformationDto submitterInformation
) {
}
