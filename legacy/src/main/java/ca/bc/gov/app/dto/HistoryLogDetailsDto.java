package ca.bc.gov.app.dto;

public record HistoryLogDetailsDto(
    String columnName,
    String oldValue, 
    String newValue
) {

}
