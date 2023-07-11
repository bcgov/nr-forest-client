package ca.bc.gov.app.dto.client;

/**
 * Request object to send mails to CHES
 * @param incorporation the incorporation number
 * @param name the name of the client
 * @param userId the user id
 * @param userName the name of the user, don't mistake with the name of the client or the username
 * @param mail the mail to send to
 */
public record SendMailRequestDto(
    String incorporation,
    String name,
    String userId,
    String userName,
    String mail
) {
}
