package ca.bc.gov.app.dto.client;

import java.util.Map;
import lombok.With;

/**
 * Represents a client submission request containing business and location details.
 *
 * <p>This record encapsulates client-related information required for a submission, including
 * business details, location, user ID, and a flag indicating whether the client should be 
 * notified.
 *
 * @param businessInformation The business information of the client.
 * @param location            The location details of the client.
 * @param userId              The ID of the user making the submission.
 * @param notifyClientInd     Indicator specifying whether the client should be notified.
 */
@With
public record ClientSubmissionDto(
    ClientBusinessInformationDto businessInformation,
    ClientLocationDto location,
    String userId,
    String notifyClientInd) {
  /**
   * Returns a map containing the description of the client's business information.
   *
   * @return a map with keys representing the description fields and corresponding values
   */
  public Map<String, Object> description(String userName) {
    Map<String, Object> descriptions = location.description();
    descriptions.put("business", businessInformation.description());
    descriptions.put("userName", userName);
    return descriptions;
  }

}
