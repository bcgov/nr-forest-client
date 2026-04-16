package ca.bc.gov.app.dto;

import java.time.LocalDate;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

/**
 * Data transfer object that encapsulates the optional search criteria used by
 * the advanced client search endpoint.
 *
 * <p>All fields are optional – when a field is {@code null} or blank it is
 * ignored by the search query. Non-blank fields are combined with AND logic
 * to narrow down the result set.
 *
 * @param clientName           the client's last name or organization name to search for
 * @param firstName            the client's first name to search for
 * @param middleName           the client's middle name to search for
 * @param clientStatus         the client status code (e.g. {@code "ACT"}, {@code "DAC"})
 * @param clientType           the client type code (e.g. {@code "I"} for individual,
 *                             {@code "C"} for corporation)
 * @param clientIdType         the type of the client identification document
 * @param clientIdentification the client identification number
 * @param emailAddress         the email address associated with the client contact or location
 * @param contactName          the name of a contact person associated with the client
 * @param userId               the user ID performing the search
 * @param updatedFromDate      the lower bound for the last updated date (inclusive)
 * @param updatedToDate        the upper bound for the last updated date (inclusive)
 */
public record ClientAdvancedSearchCriteriaDto(
    String clientName, 
    String firstName, 
    String middleName,
    String clientStatus, 
    String clientType, 
    String clientIdType,
    String clientIdentification, 
    String emailAddress,
    String contactName,
    String userId,
    LocalDate updatedFromDate, 
    LocalDate updatedToDate
) {
  
  /**
   * Checks whether at least one search parameter contains a non-blank value.
   *
   * <p>This method first {@linkplain #sanitized() sanitizes} the criteria
   * (converting blank strings to {@code null}) and then checks if any field
   * is non-null.
   *
   * @return {@code true} if at least one search field is non-blank;
   *         {@code false} otherwise
   */
  public boolean hasValidParams() {
    ClientAdvancedSearchCriteriaDto sanitizedCriteria = sanitized();
    return Stream
        .of(
            sanitizedCriteria.clientName, 
            sanitizedCriteria.firstName, 
            sanitizedCriteria.middleName,
            sanitizedCriteria.clientStatus, 
            sanitizedCriteria.clientType, 
            sanitizedCriteria.clientIdType,
            sanitizedCriteria.clientIdentification, 
            sanitizedCriteria.emailAddress,
            sanitizedCriteria.contactName,
            sanitizedCriteria.userId,
            sanitizedCriteria.updatedFromDate,
            sanitizedCriteria.updatedToDate)
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
        blankToNull(contactName),
        blankToNull(userId),
        blankToNull(updatedFromDate),
        blankToNull(updatedToDate)
    );
  }

  /**
   * Returns {@code null} if the given value is a {@link String} that is blank;
   * otherwise returns the original value unchanged.
   *
   * <p>A value is considered blank if it is {@code null}, empty, or contains only
   * whitespace (as defined by {@code StringUtils.isBlank}).
   *
   * <p>For non-{@link String} types (e.g., {@link java.time.LocalDateTime}),
   * the value is returned as-is.
   *
   * @param <T> the type of the input value
   * @param value the value to check
   * @return {@code null} if the value is a blank {@link String}; otherwise the original value
   */
  private static <T> T blankToNull(T value) {
    if (value instanceof String str) {
      return StringUtils.isBlank(str) ? null : value;
    }
    return value;
  }
}