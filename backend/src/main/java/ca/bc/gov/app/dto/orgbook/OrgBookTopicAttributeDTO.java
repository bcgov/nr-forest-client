package ca.bc.gov.app.dto.orgbook;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TopicAttribute")
public record OrgBookTopicAttributeDTO(
    Integer id,
    @JsonProperty("type") String topicAttributeType,
    String format,
    String value,
    @JsonProperty("credentialId") String credentialId,
    @JsonProperty("credential_type_id") String credentialTypeId
) {
}
