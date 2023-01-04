package ca.bc.gov.app.dto.orgbook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TopicName")
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrgBookTopicNameDto(
    Integer id,
    String text,
    String language,
    @JsonProperty("type") String topicNameType,
    @JsonProperty("credential_id") Integer credentialId
) {
}
