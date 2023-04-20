package ca.bc.gov.app.dto.ches;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "content",
    "contentType",
    "encoding",
    "filename"
})
public record ChesMailAttachment(

    String content,
    String contentType,
    ChesMailEncoding encoding,
    String filename
) {
}
