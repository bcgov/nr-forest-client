package ca.bc.gov.app.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.With;

@Schema(
    description = "An address information",
    title = "ClientAddressData",
    example = """
        {
          "streetAddress": "123 Main St",
          "country": {
            "value": "CA",
            "text": "Canada"
          },
          "province": {
            "value": "ON",
            "text": "Ontario"
          },
          "city": "Toronto",
          "postalCode": "M5V2L7",
          "index": 1,
          "contacts":{
              "type": "person",
              "firstName": "JAMES",
              "lastName": "BAXTER",
              "phoneNumber": "123456789"
              "email": "james@email.ca",
          }
        }"""
)
@With
public record ClientAddressDto(
    @Schema(description = "The street address", example = "123 Main St")
    String streetAddress,
    @Schema(description = "The country for this address", example = """
        {
          "value": "CA",
          "text": "Canada"
        }""")
    ClientValueTextDto country,
    @Schema(description = "The province or state for this address", example = """
        {
            "value": "ON",
            "text": "Ontario"
        }""")
    ClientValueTextDto province,
    @Schema(description = "The city for this address", example = "Toronto")
    String city,
    @Schema(description = "The postal code or zip code for this address", example = "M5V2L7")
    String postalCode,
    @Schema(description = "The index for this address. It is used to order the addresses",
        example = "3")
    int index,

    @Schema(description = "A list of contacts for this address",example = """
         {
          "type": "person",
          "firstName": "JAMES",
          "lastName": "BAXTER",
          "phoneNumber": "123456789"
          "email": "james@email.ca",
        }""")
    List<ClientContactDto> contacts
) {
}
