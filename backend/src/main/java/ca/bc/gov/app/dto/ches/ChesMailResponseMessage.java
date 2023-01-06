package ca.bc.gov.app.dto.ches;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "msgId",
    "tag",
    "to"
})
public record ChesMailResponseMessage(
    @JsonPropertyDescription("A corresponding message uuid")
    @NotNull
    UUID msgId,

    @JsonPropertyDescription("A unique string which is associated with the message")
    String tag,

    @JsonPropertyDescription("An array of recipient email addresses that this message will go to")
    @Valid
    @NotNull
    List<String> to
) {
}
