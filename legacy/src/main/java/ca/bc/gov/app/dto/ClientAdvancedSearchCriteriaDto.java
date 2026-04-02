package ca.bc.gov.app.dto;

import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public record ClientAdvancedSearchCriteriaDto(
    String clientName, 
    String firstName, 
    String middleName,
    String clientStatus, 
    String clientType, 
    String clientIdType,
    String clientIdentification, 
    String locationEmail,
    String contactName, 
    String contactEmail
) {
  public boolean hasValidParams() {
    return Stream
        .of(clientName, 
            firstName, 
            middleName, 
            clientStatus, 
            clientType, 
            clientIdType,
            clientIdentification, 
            locationEmail, 
            contactName, 
            contactEmail)
        .anyMatch(StringUtils::isNotBlank);
  }
}
