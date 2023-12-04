package ca.bc.gov.app.dto.client;

import java.util.Map;

public record EmailLogDto(
    Integer emailLogId,
    String templateName, 
    String emailAddress, 
    String subject,
    String emailSentInd, 
    String emailId, 
    String exceptionMessage,
    Map<String, Object> variables
) {
}
