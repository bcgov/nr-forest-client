package ca.bc.gov.app.dto.client;

public record ClientBusinessInformationDto(
    String firstName,
    String lastName,
    String birthdate,
    String incorporationNumber,
    String doingBusinessAsName,
    String businessName,
    ClientTypeEnum clientType,
    BusinessTypeEnum businessType,
    LegalTypeEnum legalType,
    String goodStanding
) {
}
