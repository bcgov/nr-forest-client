package ca.bc.gov.app.dto.client;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.With;
import org.apache.commons.lang3.StringUtils;

@With
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
    ClientValueTextDto identificationType,
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
    descMap.put("registrationNumber", StringUtils.defaultString(registrationNumber));
    descMap.put("name", StringUtils.defaultString(businessName));
    descMap.put("businessType", StringUtils.defaultString(businessType));
    descMap.put("clientType", StringUtils.defaultString(clientType));
    descMap.put("goodStanding", StringUtils.defaultString(goodStandingInd));
    descMap.put("legalType", StringUtils.defaultString(legalType));
    descMap.put("birthdate", Optional.ofNullable(birthdate).orElse(LocalDate.of(1975, 1, 31)));
    descMap.put("district", StringUtils.defaultString(district));
    descMap.put("workSafeBcNumber", StringUtils.defaultString(workSafeBcNumber));
    descMap.put("doingBusinessAs", StringUtils.defaultString(doingBusinessAs));
    descMap.put("clientAcronym", StringUtils.defaultString(clientAcronym));
    descMap.put("firstName", StringUtils.defaultString(firstName));
    descMap.put("middleName", StringUtils.defaultString(middleName));
    descMap.put("lastName", StringUtils.defaultString(lastName));
    descMap.put("notes", StringUtils.defaultString(notes));
    descMap.put("identificationType", StringUtils.defaultString(idType()));
    descMap.put("clientIdentification", StringUtils.defaultString(clientIdentification));
    descMap.put("identificationCountry", StringUtils.defaultString(identificationCountry));
    descMap.put("identificationProvince", StringUtils.defaultString(identificationProvince));
    return descMap;
  }

  /**
   * This method is used to determine the identification type of the client.
   * <p>
   * It first checks if both the identificationType and clientIdentification fields are not blank.
   * If they are not, it then checks if the identificationType is either CDDL or USDL and if the
   * identificationProvince is not blank. If these conditions are met, it returns the
   * identificationProvince concatenated with "DL". If these conditions are not met, it simply
   * returns the identificationType.
   * <p>
   * If either the identificationType or clientIdentification fields are blank, it returns null.
   *
   * @return The identification type of the client, or null if the necessary fields are blank.
   */
  public String idType() {

    if (
        identificationType != null
            &&
        StringUtils.isNotBlank(identificationType.value())
            && StringUtils.isNotBlank(clientIdentification)
    ) {

      if (
          (
              IdentificationTypeEnum.CDDL.equals(IdentificationTypeEnum.valueOf(identificationType.value()))
                  &&
                  IdentificationTypeEnum.USDL.equals(
                      IdentificationTypeEnum.valueOf(identificationType.value()))
          )
              && StringUtils.isNotBlank(identificationProvince)) {
        return identificationProvince + "DL";
      }

      return identificationType.value();
    }
    return null;
  }

}
