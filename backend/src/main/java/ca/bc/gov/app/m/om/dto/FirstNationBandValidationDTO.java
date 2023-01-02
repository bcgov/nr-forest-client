package ca.bc.gov.app.m.om.dto;

public record FirstNationBandValidationDTO(
    String clientNumber,
    String corpRegnNmbr,
    String clientName,
    String sourceClientName,
    Boolean nameMatch,
    String addressOne,
    String sourceAddressOne,
    String addressTwo,
    String sourceAddressTwo,
    String city,
    String sourceCity,
    String province,
    String sourceProvince,
    String postalCode,
    String sourcePostalCode,
    Boolean addressMatch
) {
  public FirstNationBandValidationDTO {
    nameMatch = clientName.equalsIgnoreCase(sourceClientName);
    addressMatch = ((addressOne != null && sourceAddressOne != null
        && addressOne.equalsIgnoreCase(sourceAddressOne.replace(".", "")))
        || (addressTwo != null && sourceAddressTwo != null
        && addressTwo.equalsIgnoreCase(sourceAddressTwo.replace(".", ""))))
        && (city != null && city.equalsIgnoreCase(sourceCity))
        &&
        (postalCode != null && postalCode.equalsIgnoreCase(sourcePostalCode.replaceAll("\\s", "")));
  }

}
