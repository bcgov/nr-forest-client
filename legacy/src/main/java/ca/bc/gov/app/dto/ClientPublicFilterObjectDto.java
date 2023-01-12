package ca.bc.gov.app.dto;

public record ClientPublicFilterObjectDto(
    String clientName,
    String clientFirstName,
    String clientMiddleName,
    String clientTypeCodesAsCsv,
    int currentPage,
    int itemsPerPage
) {
}
