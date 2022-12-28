package ca.bc.gov.app.dto.orgbook;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NamedResult")
public record OrgBookNameDTO(
    @JsonProperty("type") String resultType,
    @JsonProperty("sub_type") String resultSubType,

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
    String topicSourceId,

    @Schema(
        name = "topic_type",
        title = "The type of the content returned",
        example = "registration.registries.ca"
    )
    @JsonProperty("topic_type")
    String topicType,

    @Schema(
        name = "credentialType",
        title = "Tye type of the credential returned",
        example = "registration.registries.ca"
    )
    @JsonProperty("credentialType")
    String credentialType,

    @Schema(
        name = "credentialId",
        title = "Tye id of the credential returned",
        example = "d759d210-628a-4a02-9da3-506cb253ee6c"
    )
    @JsonProperty("credentialId")
    String credentialId,

    @Schema(
        example = "59.46851"
    )
    Float score
) {
}
