package ca.bc.gov.app.dto.orgbook;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.ZonedDateTime;
import java.util.List;

@Schema(name = "TopicResult")
public record OrgBookTopicDTO(
    Integer id,
    @JsonProperty("source_id") String sourceId,
    @JsonProperty("type") String topicType,
    List<OrgBookTopicNameDTO> names,
    List<OrgBookTopicAddressDTO> addresses,
    List<OrgBookTopicAttributeDTO> attributes,

    @JsonProperty("credential_set") OrgBookCredentialSetDTO credentialSet,
    @JsonProperty("credentialType") OrgBookCredentialTypeDTO credentialType,
    Boolean inactive,
    Boolean revoked,
    @JsonProperty("effective_date") ZonedDateTime effectiveAt,
    @JsonProperty("revoked_date") ZonedDateTime revokedAt
) {
}
