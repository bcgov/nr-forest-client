package ca.bc.gov.app.dto.ches;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * The type Ches request dto.
 *
 * @param emailTo   list of emails to send message to
 * @param emailBody body of the email, can be html
 */
public record ChesRequestDto(
    @NotNull
    @Min(1)
    @Email
    List<String> emailTo,
    @NotNull
    String emailBody
) {

}
