package ca.bc.gov.app.dto.client;

public record ClientBusinessInformationDto(
    String incorporationNumber,
    String businessName,
    BusinessTypeEnum businessType,
    ClientTypeEnum clientType,
    String goodStanding,
    LegalTypeEnum legalType
) {
}
