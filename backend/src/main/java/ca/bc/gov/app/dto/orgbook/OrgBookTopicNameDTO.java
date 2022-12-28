package ca.bc.gov.app.dto.orgbook;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TopicName")
public record OrgBookTopicNameDTO(
    Integer id,
    String text,
    String language,
    @JsonProperty("type") String topicNameType,
    @JsonProperty("credentialId") String credentialId
) {
}
