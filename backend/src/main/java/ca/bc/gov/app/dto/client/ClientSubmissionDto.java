package ca.bc.gov.app.dto.client;

import java.util.Map;
import lombok.With;

@With
public record ClientSubmissionDto(
    ClientBusinessInformationDto businessInformation,
    ClientLocationDto location,
    String userId) {
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
