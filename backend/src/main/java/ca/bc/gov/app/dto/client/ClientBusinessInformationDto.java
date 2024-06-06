package ca.bc.gov.app.dto.client;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public record ClientBusinessInformationDto(
    String registrationNumber, 
    String businessName,
    String firstName,
    String middleName,
    String businessType, 
    String clientType, 
    String goodStandingInd, 
    String legalType,
    LocalDate birthdate, 
    String district,
    String idType,
    String idValue
) {
  /**
   * Returns a map containing the description of the client's business information.
   *
   * @return a map with keys representing the description fields and corresponding values
   */
  public Map<String, Object> description() {
    return Map.of(
            "registrationNumber", StringUtils.isBlank(registrationNumber) ? "" : registrationNumber, 
            "name", StringUtils.isBlank(businessName) ? "" : businessName,
            "businessType", StringUtils.isBlank(businessType) ? "" : businessType, 
            "clientType", StringUtils.isBlank(clientType) ? "" : clientType, 
            "goodStanding", StringUtils.isBlank(goodStandingInd) ? "" : goodStandingInd, 
            "legalType", StringUtils.isBlank(legalType) ? "" : legalType, 
            "birthdate", Optional.ofNullable(birthdate).isPresent() ? birthdate : LocalDate.of(1975, 1, 31),
            "district", StringUtils.isBlank(district) ? "" : district
    );
  }
}
