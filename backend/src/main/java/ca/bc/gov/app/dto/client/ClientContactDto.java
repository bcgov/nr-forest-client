package ca.bc.gov.app.dto.client;

public record ClientContactDto(
    String contactType,
    String firstName,
    String lastName,
    String businessPhone,
    String email,
    int index
) {
}
