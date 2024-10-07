package ca.bc.gov.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public record PredictiveSearchResultDto(
    String clientNumber,
    String clientAcronym,
    String clientName,
    String clientFirstName,
    String doingBusinessAs,
    String clientIdentification,
    String clientMiddleName,
    String city,
    String clientType,
    String statusCode,
    long score
) {

  @JsonProperty("name")
  public String name() {
    String finalName = Stream.of(this.clientFirstName, this.clientMiddleName, this.clientName)
        .filter(StringUtils::isNotBlank)
        .map(String::trim)
        .collect(Collectors.joining(" "));

    return StringUtils.isNotBlank(this.doingBusinessAs)
        ? finalName + " (" + this.doingBusinessAs + ")"
        : finalName;
  }

}