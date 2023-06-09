package ca.bc.gov.app.dto.client;

import java.util.Map;

public record ClientSubmissionDto(
    ClientBusinessInformationDto businessInformation,
    ClientLocationDto location) {
  public Map<String, Object> description(String userName) {
    Map<String, Object> descriptions = location.description();
    // TODO: Add here other values
    descriptions.put("business", businessInformation.description());
    descriptions.put("userName", userName);
    return descriptions;
  }

}
