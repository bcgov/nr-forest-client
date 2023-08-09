package ca.bc.gov.app.dto.client;

import java.util.Map;

/**
 * Request object to send mails to CHES
 * @param incorporation the incorporation number
 * @param name the name of the client
 * @param userId the user id
 * @param userName the name of the user, don't mistake with the name of the client or the username
 * @param email the mail to send to
 */
public record EmailRequestDto(
    String incorporation,
    String name,
    String userId,
    String userName,
    String email,
    String templateName,
    String subject,
    Map<String, Object> variables
) {
}
