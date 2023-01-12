package ca.bc.gov.app.dto;

import org.apache.commons.lang3.StringUtils;

public record FirstNationBandVidationDto(
    String clientNumber,
    String corpRegnNmbr,
    String clientName,
    String sourceClientName,
    String addressOne,
    String sourceAddressOne,
    String addressTwo,
    String sourceAddressTwo,
    String city,
    String sourceCity,
    String province,
    String sourceProvince,
    String postalCode,
    String sourcePostalCode
) {
  public boolean nameMatch() {
    return StringUtils.equalsIgnoreCase(clientName, sourceClientName);
  }

  public boolean addressMatch() {

    return
        (
            (
                StringUtils.isNotBlank(addressOne)
                    && StringUtils.isNotBlank(sourceAddressOne)
                    && addressOne.equalsIgnoreCase(
                    sourceAddressOne.replace(".", "")
                )
            )
                || (
                StringUtils.isNotBlank(addressTwo)
                    && StringUtils.isNotBlank(sourceAddressTwo)
                    && addressTwo.equalsIgnoreCase(
                    sourceAddressTwo.replace(".", "")
                )
            )
        )
            && StringUtils.equalsIgnoreCase(city, sourceCity)
            && (
            StringUtils.isNotBlank(postalCode)
                && postalCode.equalsIgnoreCase(
                sourcePostalCode.replaceAll("\\s", "")
            )
        );
  }
}
