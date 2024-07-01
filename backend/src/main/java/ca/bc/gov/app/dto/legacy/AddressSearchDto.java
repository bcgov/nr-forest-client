package ca.bc.gov.app.dto.legacy;

import org.apache.commons.lang3.StringUtils;

public record AddressSearchDto(
    String address,
    String city,
    String province,
    String postalCode,
    String country
) {

  public boolean isValid() {
    return !StringUtils.isAnyBlank(address, city, province, postalCode, country);
  }

}
