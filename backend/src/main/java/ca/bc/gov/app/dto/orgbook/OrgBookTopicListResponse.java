package ca.bc.gov.app.dto.orgbook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "TopicListResponse", description = "A list of Topics")
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrgBookTopicListResponse(
    @Schema(description = "The total amount of entries on all pages", example = "75")
    int total,
    @Schema(description = "The zero-index current page number", example = "3")
    Integer page,

    @Schema(description = "The amount of entries on this page", example = "3")
    @JsonProperty("page_size")
    Integer pageSize,

    @Schema(description = "The list of topic results")
    List<OrgBookTopicDto> results
) {
}