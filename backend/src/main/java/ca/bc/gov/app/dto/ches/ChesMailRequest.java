package ca.bc.gov.app.dto.ches;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "attachments",
    "bcc",
    "bodyType",
    "body",
    "cc",
    "delay",
    "encoding",
    "from",
    "priority",
    "subject",
    "tag",
    "to"
})
public record ChesMailRequest(

    @JsonPropertyDescription("An array of Attachment objects")
    @Valid
    List<ChesMailAttachment> attachments,

    @JsonPropertyDescription("An array of recipients contactEmail addresses "
        + "that will appear on the BCC: field")
    @Valid
    List<String> bcc,

    @JsonPropertyDescription("The contactEmail body type (html = content with html, text = plaintext)")
    @NotNull
    ChesMailBodyType bodyType,

    @JsonPropertyDescription("The body of the message as an Unicode string")
    @NotNull
    String body,

    @JsonPropertyDescription("An array of recipients contactEmail addresses "
        + "that will appear on the CC: field")
    @Valid
    List<String> cc,

    @JsonPropertyDescription("Desired UTC time for sending the message. "
        + "0 = Queue to send immediately")
    @JsonProperty("delayTs")
    Integer delay,

    @JsonPropertyDescription("Identifies encoding for text/html strings "
        + "(defaults to 'utf-8', other values are 'hex' and 'base64')")
    ChesMailEncoding encoding,

    @JsonPropertyDescription("The contactEmail address of the sender. "
        + "All contactEmail addresses can be plain 'sender@server.com' or "
        + "formatted '\"Sender Name\" <sender@server.com>'")
    @NotNull
    String from,

    @JsonPropertyDescription("Sets message importance headers, either "
        + "'high', 'normal' (default) or 'low'.")
    ChesMailPriority priority,

    @JsonPropertyDescription("The contactEmail subject")
    @NotNull
    String subject,

    @JsonPropertyDescription("A unique string which is associated with the message")
    String tag,

    @JsonPropertyDescription("An array of recipients contactEmail addresses "
        + "that will appear on the To: field")
    @Valid
    @NotNull
    List<String> to
) {
}
