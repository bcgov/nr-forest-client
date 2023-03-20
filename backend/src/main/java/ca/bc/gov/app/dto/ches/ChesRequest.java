package ca.bc.gov.app.dto.ches;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(name = "MailRequest")
public record ChesRequest(
    @NotNull
    @Min(1)
    @Email
    @Schema(name = "emailTo",
        description = "An array of recipients email addresses",
        example = "foo@bar.com")
    List<String> emailTo,
    @NotNull
    @Schema(name = "emailBody",
        description = "The body of the message as an Unicode HTML string",
        example = "<p>Hello</p>")
    String emailBody
) {
}
