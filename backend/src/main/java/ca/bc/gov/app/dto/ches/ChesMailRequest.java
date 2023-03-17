package ca.bc.gov.app.dto.ches;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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

    List<ChesMailAttachment> attachments,
    List<String> bcc,
    ChesMailBodyType bodyType,
    String body,
    List<String> cc,
    Integer delay,
    ChesMailEncoding encoding,
    String from,
    ChesMailPriority priority,
    String subject,
    String tag,
    List<String> to
) {
}
