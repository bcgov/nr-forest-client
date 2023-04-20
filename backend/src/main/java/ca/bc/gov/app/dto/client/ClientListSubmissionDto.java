package ca.bc.gov.app.dto.client;

public record ClientListSubmissionDto(
    long id,
    String requestType,
    String name,
    String clientType,
    String updated,
    String status
) {
}
