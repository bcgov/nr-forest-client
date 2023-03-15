package ca.bc.gov.app.dto.orgbook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrgBookNameDto(

    String value,
    @JsonProperty("sub_type")
    String subType,
    @JsonProperty("topic_source_id")
    String topicSourceId,
    List<String> names
) {
}
