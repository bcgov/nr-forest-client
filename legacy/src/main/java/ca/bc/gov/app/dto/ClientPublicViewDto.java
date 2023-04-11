package ca.bc.gov.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

public record ClientPublicViewDto(
    String clientNumber,
    String clientName,
    String incorporationNumber,

    String orgBookNumber,

    String orgBookName,
    boolean status

) {

  @JsonProperty
  public String sameName() {
    return
        BooleanUtils.toStringYesNo(
            StringUtils.equalsIgnoreCase(orgBookName, clientName)
        );
  }

  @JsonProperty
  public String sameNumber() {
    return
        BooleanUtils.toStringYesNo(
            StringUtils.equalsIgnoreCase(orgBookNumber, incorporationNumber)
        );
  }

  @JsonProperty
  public String found() {
    return
        BooleanUtils.toString(
            StringUtils.equalsIgnoreCase(orgBookName, clientName)
                && StringUtils.equalsIgnoreCase(orgBookNumber, incorporationNumber),
            "F",
            BooleanUtils.toString(
                StringUtils.equalsIgnoreCase(orgBookName, clientName)
                    || StringUtils.equalsIgnoreCase(orgBookNumber, incorporationNumber),
                "PF",
                "NF"
            )
        );
  }

  @JsonProperty
  public String active() {
    return BooleanUtils.toStringYesNo(status);
  }

}
