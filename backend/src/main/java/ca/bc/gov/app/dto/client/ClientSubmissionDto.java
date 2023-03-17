package ca.bc.gov.app.dto.client;

public record ClientSubmissionDto(
    ClientBusinessTypeDto businessType,
    ClientBusinessInformationDto businessInformation,
    ClientLocationDto location,
    ClientSubmitterInformationDto submitterInformation
) {
}
