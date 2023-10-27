package ca.bc.gov.app.dto.cognito;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record RefreshRequestDto(
    @JsonProperty("ClientId")
    String clientId,
    @JsonProperty("AuthFlow")
    String authFlow,
    @JsonProperty("AuthParameters")
    Map<String,Object> authParameters
) {
}
