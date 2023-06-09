package ca.bc.gov.app.dto.ches;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CommonExposureJwtDto(

    @JsonProperty("access_token") String accessToken,
    @JsonProperty("expires_in") long expiresIn,
    @JsonProperty("refresh_expires_in") long refreshExpiresIn,
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("not-before-policy") long notBeforePolicy,
    @JsonProperty("scope") String scope
) {
}
