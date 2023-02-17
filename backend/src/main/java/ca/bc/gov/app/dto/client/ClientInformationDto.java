package ca.bc.gov.app.dto.client;

public record ClientInformationDto(
    String firstName,
    String lastName,
    String birthdate,
    String incorporationNumber,
    String doingBusinessAsName,
    String businessName
) {
}
