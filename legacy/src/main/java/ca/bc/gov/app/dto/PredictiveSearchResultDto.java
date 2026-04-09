package ca.bc.gov.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

/**
 * Data transfer object representing a single result row returned by the
 * predictive or advanced client search endpoints.
 *
 * <p>Includes core client identifiers, name components, doing-business-as
 * information, location details, and a relevance score used for ordering
 * results.
 *
 * @param clientNumber         the unique client number
 * @param clientAcronym        the client acronym, if any
 * @param clientName           the client's last name or organization name
 * @param clientFirstName      the client's first name (individuals only)
 * @param doingBusinessAs      the "doing business as" name, if applicable
 * @param clientIdentification the identification number associated with the client
 * @param clientMiddleName     the client's middle name (individuals only)
 * @param city                 the city of the client's primary location
 * @param clientType           the client type code (e.g. {@code "I"}, {@code "C"})
 * @param clientStatus         the client status code (e.g. {@code "ACT"}, {@code "DAC"})
 * @param score                a relevance score used for ordering search results
 */
public record PredictiveSearchResultDto(
    String clientNumber,
    String clientAcronym,
    String clientName,
    String clientFirstName,
    String doingBusinessAs,
    String clientIdentification,
    String clientMiddleName,
    String city,
    String clientType,
    String clientStatus,
    long score
) {

  /**
   * Builds the client's full display name by joining the first, middle, and
   * last name components, trimming whitespace and skipping blank parts.
   *
   * <p>If a {@link #doingBusinessAs} value is present it is appended in
   * parentheses – for example {@code "John Smith (ACME Trading)"}.
   *
   * @return the formatted full name of the client, never {@code null}
   */
  @JsonProperty("clientFullName")
  public String clientFullName() {
    String finalName = Stream.of(this.clientFirstName, this.clientMiddleName, this.clientName)
        .filter(StringUtils::isNotBlank)
        .map(String::trim)
        .collect(Collectors.joining(" "));

    return StringUtils.isNotBlank(this.doingBusinessAs)
        ? finalName + " (" + this.doingBusinessAs + ")"
        : finalName;
  }

}
