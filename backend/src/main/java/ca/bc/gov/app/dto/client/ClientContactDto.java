package ca.bc.gov.app.dto.client;

public record ClientContactDto(
    String contactType,
    String name,
    String businessPhone,
    String email,
    int index
) {
}
