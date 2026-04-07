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
    String emailAddress,
    String contactName
) {
  
  public boolean hasValidParams() {
    ClientAdvancedSearchCriteriaDto sanitizedCriteria = sanitized();
    return Stream
        .of(sanitizedCriteria.clientName, 
            sanitizedCriteria.firstName, 
            sanitizedCriteria.middleName,
            sanitizedCriteria.clientStatus, 
            sanitizedCriteria.clientType, 
            sanitizedCriteria.clientIdType,
            sanitizedCriteria.clientIdentification, 
            sanitizedCriteria.emailAddress,
            sanitizedCriteria.contactName)
        .anyMatch(java.util.Objects::nonNull);
  }

  /**
   * Returns a new instance with all blank/empty values converted to null,
   * so the SQL query can skip them via {@code :param IS NULL}.
   */
  public ClientAdvancedSearchCriteriaDto sanitized() {
    return new ClientAdvancedSearchCriteriaDto(
        blankToNull(clientName),
        blankToNull(firstName),
        blankToNull(middleName),
        blankToNull(clientStatus),
        blankToNull(clientType),
        blankToNull(clientIdType),
        blankToNull(clientIdentification),
        blankToNull(emailAddress),
        blankToNull(contactName)
    );
  }

  private static String blankToNull(String value) {
    return StringUtils.isBlank(value) ? null : value;
  }
}
