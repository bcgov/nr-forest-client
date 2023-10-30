package ca.bc.gov.app.dto.cognito;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record RefreshResponseDto(
    @JsonProperty("AuthenticationResult")
    RefreshResponseResultDto result,
    @JsonProperty("ChallengeParameters")
    Map<String,Object> challengeParams) {
}
