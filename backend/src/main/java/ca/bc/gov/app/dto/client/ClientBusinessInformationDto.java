package ca.bc.gov.app.dto.client;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public record ClientBusinessInformationDto(
    String registrationNumber, 
    String businessName,
    String businessType, 
    String clientType, 
    String goodStandingInd, 
    String legalType,
    LocalDate birthdate, 
    String district,
    String workSafeBcNumber,
    String doingBusinessAs,
    String clientAcronym,
    String firstName,
    String middleName,
    String lastName,
    String notes,
    String identificationType,
    String clientIdentification,
    String identificationCountry,
    String identificationProvince) {
  /**
   * Returns a map containing the description of the client's business information.
   *
   * @return a map with keys representing the description fields and corresponding values
   */
  public Map<String, Object> description() {
    Map<String, Object> descMap = new HashMap<>();
    descMap.put("registrationNumber", StringUtils.isBlank(registrationNumber) ? "" : registrationNumber);
    descMap.put("name", StringUtils.isBlank(businessName) ? "" : businessName);
    descMap.put("businessType", StringUtils.isBlank(businessType) ? "" : businessType);
    descMap.put("clientType", StringUtils.isBlank(clientType) ? "" : clientType);
    descMap.put("goodStanding", StringUtils.isBlank(goodStandingInd) ? "" : goodStandingInd);
    descMap.put("legalType", StringUtils.isBlank(legalType) ? "" : legalType);
    descMap.put("birthdate", Optional.ofNullable(birthdate).orElse(LocalDate.of(1975, 1, 31)));
    descMap.put("district", StringUtils.isBlank(district) ? "" : district);
    descMap.put("workSafeBcNumber", StringUtils.isBlank(workSafeBcNumber) ? "" : workSafeBcNumber);
    descMap.put("doingBusinessAs", StringUtils.isBlank(doingBusinessAs) ? "" : doingBusinessAs);
    descMap.put("clientAcronym", StringUtils.isBlank(clientAcronym) ? "" : clientAcronym);
    descMap.put("firstName", StringUtils.isBlank(firstName) ? "" : firstName);
    descMap.put("middleName", StringUtils.isBlank(middleName) ? "" : middleName);
    descMap.put("lastName", StringUtils.isBlank(lastName) ? "" : lastName);
    descMap.put("notes", StringUtils.isBlank(notes) ? "" : notes);
    descMap.put("identificationType", StringUtils.isBlank(identificationType) ? "" : identificationType);
    descMap.put("clientIdentification", StringUtils.isBlank(clientIdentification) ? "" : clientIdentification);
    descMap.put("identificationCountry", StringUtils.isBlank(identificationCountry) ? "" : identificationCountry);
    descMap.put("identificationProvince", StringUtils.isBlank(identificationProvince) ? "" : identificationProvince);
    return descMap;
  }
}
