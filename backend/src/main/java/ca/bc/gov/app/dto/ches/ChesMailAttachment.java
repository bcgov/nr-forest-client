package ca.bc.gov.app.dto.ches;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "content",
    "contentType",
    "encoding",
    "filename"
})
public record ChesMailAttachment(

    @JsonPropertyDescription("String, Buffer or a Stream contents for the attachment")
    String content,

    @JsonPropertyDescription("Optional content type for the attachment, "
        + "if not set will be derived from the filename property")
    String contentType,

    @JsonPropertyDescription("If set and content is string, then encodes the content to a "
        + "Buffer using the specified encoding. Example values: 'base64', 'hex', 'binary' etc. "
        + "Useful if you want to use binary attachments in a JSON formatted contactEmail object.")
    ChesMailEncoding encoding,

    @JsonPropertyDescription(
        "Filename to be reported as the name of the attached file. "
            + "Use of unicode is allowed.")
    String filename
) {
}
