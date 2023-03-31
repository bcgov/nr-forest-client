package ca.bc.gov.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrgBookTopicNameDto(
    String text,
    @JsonProperty("type") String topicNameType
) {
}