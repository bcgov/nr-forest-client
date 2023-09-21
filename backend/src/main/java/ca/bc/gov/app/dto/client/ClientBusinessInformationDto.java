package ca.bc.gov.app.dto.client;

import java.util.Map;
import lombok.With;
import org.apache.commons.lang3.StringUtils;

@With
public record ClientBusinessInformationDto(
    String incorporationNumber, 
    String businessName,
    String businessType, 
    String clientType, 
    String goodStanding, 
    String legalType) {
  /**
   * Returns a map containing the description of the client's business information.
   *
   * @return a map with keys representing the description fields and corresponding values
   */
  public Map<String, Object> description() {
    return Map.of(
        "incorporation", StringUtils.isBlank(incorporationNumber) ? "" : incorporationNumber, 
        "name", StringUtils.isBlank(businessName) ? "" : businessName, 
        "businessType", StringUtils.isBlank(businessType) ? "" : businessType, 
        "clientType", StringUtils.isBlank(clientType) ? "" : clientType,
        "goodStanding", StringUtils.isBlank(goodStanding) ? "" : goodStanding,
        "legalType", StringUtils.isBlank(legalType) ? "" : legalType
        );
  }
}
