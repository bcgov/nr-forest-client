package ca.bc.gov.app.dto.ches;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "messages",
    "txId"
})
public record ChesMailResponse(

    @JsonProperty("messages")
    @Valid
    @NotNull
    List<ChesMailResponseMessage> messages,

    @JsonPropertyDescription("A corresponding transaction uuid")
    @NotNull
    UUID txId
) {
}
