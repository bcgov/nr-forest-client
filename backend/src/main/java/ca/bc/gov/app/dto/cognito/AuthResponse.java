package ca.bc.gov.app.dto.cognito;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * AuthResponse represents the response from Cognito when authenticating a user.
 */
public record AuthResponse(
    @JsonProperty("access_token")
    String accessToken,
    @JsonProperty("expires_in")
    Integer expiresIn,
    @JsonProperty("token_type")
    String tokenType,
    @JsonProperty("refresh_token")
    String refreshToken,
    @JsonProperty("id_token")
    String idToken
) {

}
