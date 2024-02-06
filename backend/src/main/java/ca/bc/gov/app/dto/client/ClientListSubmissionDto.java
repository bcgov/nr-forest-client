package ca.bc.gov.app.dto.client;

public record ClientListSubmissionDto(
    long id,
    String requestType,
    String name,
    String clientType,
    String district,
    String updated,
    String user,
    String status,
    Long count
) {
}
