package ca.bc.gov.app.dto.client;

public record ClientContactDto(
    String contactType,
    String contactFirstName,
    String contactLastName,
    String contactPhoneNumber,
    String contactEmail
) {
}
