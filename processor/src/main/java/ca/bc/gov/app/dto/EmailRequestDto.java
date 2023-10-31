package ca.bc.gov.app.dto;

import java.util.Map;

/**
 * Represents a request object for sending emails via CHES (Common Hosted Email Service). This data
 * transfer object (DTO) encapsulates the necessary information to send an email.
 *
 * @param incorporation The incorporation number associated with the client.
 * @param name The name of the client or recipient of the email.
 * @param userId The unique identifier of the user initiating the email.
 * @param userName The name of the user initiating the email (not to be confused with the client's
 *        name or username).
 * @param email The email address to which the email will be sent.
 * @param templateName The name of the email template to be used for formatting the email content.
 * @param subject The subject of the email.
 * @param variables A map of variables to be used in the email template, allowing dynamic content.
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
