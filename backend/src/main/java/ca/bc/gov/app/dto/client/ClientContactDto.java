package ca.bc.gov.app.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;

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
public record ClientContactDto(
    @Schema(description = "The type of contact",
        example = "person")
    String contactType,

    @Schema(description = "The first name of the contact",
        example = "JAMES")
    String contactFirstName,

    @Schema(description = "Last name of the contact",
        example = "BAXTER")
    String contactLastName,

    @Schema(description = "Contact phone number", example = "555 555 5555")
    String contactPhoneNumber,

    @Schema(description = "Email address to get in touch with contact",
        example = "jhon@email.ca")
    String contactEmail
) {
}
