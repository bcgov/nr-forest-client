package ca.bc.gov.app.dto;

public record ContactAssociationDto(
    Long entityId,
    String operation,
    String oldLocationCode,
    String newLocationCode
) {

}
