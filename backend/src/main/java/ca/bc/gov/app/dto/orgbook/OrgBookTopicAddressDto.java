package ca.bc.gov.app.dto.orgbook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TopicAddress")
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrgBookTopicAddressDto(
    Integer id,
    String addressee,
    @JsonProperty("civic_address") String civicAddress,
    String city,
    String province,
    @JsonProperty("postal_code") String postalCode,
    String country,
    @JsonProperty("credentialId") String credentialId
) {
}
