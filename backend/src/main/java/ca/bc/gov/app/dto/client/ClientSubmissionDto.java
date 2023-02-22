package ca.bc.gov.app.dto.client;

public record ClientSubmissionDto(
    ClientBusinessTypeDto clientBusinessTypeDto,
    ClientInformationDto clientInformationDto,
    ClientLocationDto clientLocationDto
) {
}
