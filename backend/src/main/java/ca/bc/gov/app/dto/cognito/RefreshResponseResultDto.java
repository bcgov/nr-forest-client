package ca.bc.gov.app.dto.cognito;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RefreshResponseResultDto(
    @JsonProperty("AccessToken")
    String accessToken,
    @JsonProperty("ExpiresIn")
    Integer expiresIn,
    @JsonProperty("TokenType")
    String tokenType,
    @JsonProperty("IdToken")
    String idToken
) {

}
