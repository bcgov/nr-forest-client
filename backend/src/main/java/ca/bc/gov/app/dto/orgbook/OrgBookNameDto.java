package ca.bc.gov.app.dto.orgbook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NamedResult")
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrgBookNameDto(
    @Schema(
        name = "value",
        title = "Name of the entity being searched",
        example = "U3 POWER CORP",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String value,

    @Schema(
        name = "topic_source_id",
        title = "The incorporation ID",
        example = "BC0772006",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonProperty("topic_source_id")
    String topicSourceId
) {
}
