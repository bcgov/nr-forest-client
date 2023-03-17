package ca.bc.gov.app.dto.client;

public record ClientSubmitterInformationDto(
    String submitterFirstName,
    String submitterLastName,
    String submitterPhoneNumber,
    String submitterEmail
) {
}
