package ca.bc.gov.app.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClientValueTextDto(

    String value,

    String text
) {

}
