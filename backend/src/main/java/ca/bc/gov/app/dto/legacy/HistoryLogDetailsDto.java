package ca.bc.gov.app.dto.legacy;

public record HistoryLogDetailsDto(
    String columnName,
    String oldValue, 
    String newValue
) {

}
