package ca.bc.gov.app.dto.ches;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(name = "MailRequest")
public record ChesRequestDto(
    @NotNull
    @Min(1)
    @Email
    List<String> emailTo,
    @NotNull
    String emailBody
) {
}
