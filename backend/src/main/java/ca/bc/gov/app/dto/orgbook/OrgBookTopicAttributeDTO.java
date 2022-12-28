package ca.bc.gov.app.dto.orgbook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TopicAttribute")
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrgBookTopicAttributeDTO(
    Integer id,
    @JsonProperty("type") String topicAttributeType,
    String format,
    String value,
    @JsonProperty("credential_id") Integer credentialId,
    @JsonProperty("credential_type_id") Integer credentialTypeId
) {
}
