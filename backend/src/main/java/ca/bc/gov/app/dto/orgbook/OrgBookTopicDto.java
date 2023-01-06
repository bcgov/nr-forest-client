package ca.bc.gov.app.dto.orgbook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.ZonedDateTime;
import java.util.List;

@Schema(name = "TopicResult")
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrgBookTopicDto(
    Integer id,
    @JsonProperty("source_id") String sourceId,
    @JsonProperty("type") String topicType,
    List<OrgBookTopicNameDto> names,
    List<OrgBookTopicAddressDto> addresses,
    List<OrgBookTopicAttributeDto> attributes,

    @JsonProperty("credential_set") OrgBookCredentialSetDto credentialSet,
    @JsonProperty("credentialType") OrgBookCredentialTypeDto credentialType,
    Boolean inactive,
    Boolean revoked,
    @JsonProperty("effective_date") ZonedDateTime effectiveAt,
    @JsonProperty("revoked_date") ZonedDateTime revokedAt
) {
}
