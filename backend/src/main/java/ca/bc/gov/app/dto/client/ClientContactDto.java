package ca.bc.gov.app.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "Client information contact",
    title = "ClientContact",
    example = """
         {
          "type": "person",
          "firstName": "JAMES",
          "lastName": "BAXTER",
          "phoneNumber": "123456789"
          "email": "james@email.ca",
        }"""
)
public record ClientContactDto(
    @Schema(description = "The type of contact",
        example = "person")
    String type,

    @Schema(description = "The first name of the contact",
        example = "JAMES")
    String firstName,

    @Schema(description = "Last name of the contact",
        example = "BAXTER")
    String lastName,

    @Schema(description = "Contact phone number", example = "123456789")
    String phoneNumber,

    @Schema(description = "Email address to get in touch with contact",
        example = "jhon@email.ca")
    String email,

    int index
) {
}
