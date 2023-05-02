package ca.bc.gov.app.dto.client;

public record ClientBusinessInformationDto(
    String incorporationNumber,
    String businessName,
    String businessType,
    String clientType,
    String goodStanding,
    String legalType
) {
}
