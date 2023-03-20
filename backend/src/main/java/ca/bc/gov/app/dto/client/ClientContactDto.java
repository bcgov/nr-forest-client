package ca.bc.gov.app.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.With;

@Schema(
    description = "Client information contact",
    title = "ClientContact",
    example = """
        {
         "email": "james@email.ca",
         "firstName": "JAMES",
         "index": 0,
         "lastName": "BAXTER",
         "middleInitial": "GRAHAM",
         "contactType": "person"
       }"""
)
@With
public record ClientContactDto(
    @Schema(description = "The type of contact",
        example = "person")
    String contactType,
    @Schema(description = "The first name of the contact",
        example = "JAMES")
    String firstName,
    @Schema(description = "Last name of the contact",
        example = "BAXTER")
    String lastName,
    @Schema(description = "Contact phone number", example = "555 555 5555")
    String phoneNumber,
    @Schema(description = "Email address to get in touch with contact",
        example = "jhon@email.ca")
    String email,
    @Schema(description = "The index for this address. It is used to order the addresses",
        example = "3")
    int index
) {
}
