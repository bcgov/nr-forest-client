package ca.bc.gov.app.dto;

import org.apache.commons.lang3.StringUtils;

public record ContactSearchDto(
    String firstName,
    String middleName,
    String lastName,
    String email,
    String phone
) {

  public boolean isValid() {
    return !StringUtils.isAnyBlank(firstName, lastName, email, phone);
  }

}
