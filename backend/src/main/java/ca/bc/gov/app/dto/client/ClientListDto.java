package ca.bc.gov.app.dto.client;

public record ClientListDto(
    String clientNumber, 
    String clientAcronym, 
    String clientFullName,
    String clientType, 
    String city, 
    String clientStatus) {
}
