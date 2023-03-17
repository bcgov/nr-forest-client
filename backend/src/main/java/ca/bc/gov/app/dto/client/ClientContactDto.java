package ca.bc.gov.app.dto.client;

public record ClientContactDto(
    String contactType,
    String firstName,
    String lastName,
    String phoneNumber,
    String email,
    int index
) {
}
