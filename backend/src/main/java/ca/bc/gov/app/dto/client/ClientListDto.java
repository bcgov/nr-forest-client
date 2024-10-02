package ca.bc.gov.app.dto.client;

public record ClientListDto(
    String clientNumber, 
    String acronym, 
    String clientName,
    String clientType, 
    String city, 
    String clientStatus, 
    Long count) {
}
