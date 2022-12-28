package ca.bc.gov.app.dto.orgbook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.ZonedDateTime;

@Schema(name = "CredentialSet")
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrgBookCredentialSetDTO(
    Integer id,
    @JsonProperty("create_timestamp") ZonedDateTime createdAt,
    @JsonProperty("update_timestamp") ZonedDateTime updatedAt,
    @JsonProperty("latest_credential_id") Integer latestCredentialId,
    @JsonProperty("topic_id") Integer topicId,
    @JsonProperty("first_effective_date") ZonedDateTime firstEffectiveAt,
    @JsonProperty("last_effective_date") ZonedDateTime lastEffectiveAt
) {
}
