package ca.bc.gov.app.dto.client;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public record ClientBusinessInformationDto(String incorporationNumber, String businessName,
    String businessType, String clientType, String goodStanding, String legalType) {
  public Map<String, Object> description() {
    return Map.of(
        "incorporation", StringUtils.isBlank(incorporationNumber) ? "" : incorporationNumber, 
        "name", StringUtils.isBlank(businessName) ? "" : businessName, 
        "businessType", StringUtils.isBlank(businessType) ? "" : businessType, 
        "clientType", StringUtils.isBlank(clientType) ? "" : clientType,
        "legalType", StringUtils.isBlank(legalType) ? "" : legalType, 
        "goodStanding", StringUtils.isBlank(goodStanding) ? "" : goodStanding
        );
  }
}
