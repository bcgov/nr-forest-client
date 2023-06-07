package ca.bc.gov.app.dto.client;

import ca.bc.gov.app.dto.ValueTextDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.With;

@Schema(
    description = "Client information contact",
    title = "ClientContact",
    example = """
         {
          "contactType": {
            "value": "person",
            "text": "Person"
          },
          "firstName": "JAMES",
          "lastName": "BAXTER",
          "phoneNumber": "123456789"
          "email": "james@email.ca",
          "locations":[{
            "value": "1",
            "text": "Billing Address"
          }]
        }"""
)
@With
public record ClientContactDto(
    @Schema(description = "The type of contact",
        example = "person")
    ValueTextDto contactType,

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

    @Schema(description = "The index for this address. It is used to order the addresses",
        example = "3")
    int index,

    @Schema(description = "A list of contact to location association",
        example = """
              {
              "value": "1",
              "text": "Billing Address"
            }""")
    List<ValueTextDto> locations
) {
  public Map<String, Object> description() {
    final String indexFormatted = String.format("contact.[%d]", index);

    return
        Map.of(indexFormatted,
            Map.of(
                "firstName", firstName,
                "lastName", lastName,
                "name", String.join(" ", firstName, lastName),
                "phone", phoneNumber,
                "email", email
            )
        );
  }
}
