package ca.bc.gov.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrgBookTopicListResponse(
    int total,
    Integer page,
    @JsonProperty("page_size")
    Integer pageSize,
    List<OrgBookTopicDto> results
) {
}
