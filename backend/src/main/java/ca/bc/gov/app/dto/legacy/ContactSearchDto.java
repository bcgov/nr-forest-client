package ca.bc.gov.app.dto.legacy;

import org.apache.commons.lang3.StringUtils;

public record ContactSearchDto(
    String firstName,
    String middleName,
    String lastName,
    String email,
    String phone,
    String phone2,
    String fax
) {

  public boolean isValid() {
    return !StringUtils.isAnyBlank(firstName, lastName, email, phone);
  }

}
