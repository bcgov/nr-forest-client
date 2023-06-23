package ca.bc.gov.app.dto.client;

public record SendMailRequestDto(
    String incorporation,
    String name,
    String userId,
    String mail
) {
}
