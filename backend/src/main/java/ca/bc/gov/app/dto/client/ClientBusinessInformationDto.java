package ca.bc.gov.app.dto.client;

import java.util.Map;

public record ClientBusinessInformationDto(
    String incorporationNumber,
    String businessName,
    String businessType,
    String clientType,
    String goodStanding,
    String legalType
) {
  public Map<String, Object> description() {
    /*return Map.of(
        "incorporation", incorporationNumber,
        "name", businessName,
        "businessType", businessType,
        "clientType", clientType,
        "legalType", legalType,
        "goodStanding", goodStanding
    );*/
    return Map.of(
        "incorporation", "",
        "name", "",
        "businessType", "",
        "clientType", "",
        "legalType", "",
        "goodStanding", ""
    );
  }
}
