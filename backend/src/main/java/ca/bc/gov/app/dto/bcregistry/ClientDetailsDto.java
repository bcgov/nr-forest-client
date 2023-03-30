package ca.bc.gov.app.dto.bcregistry;

import ca.bc.gov.app.dto.client.ClientAddressDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.With;

/**
 * A data transfer object representing the details of a client.
 */
@Schema(
    example = """
        {
          "name": "Acme Inc.",
          "id": "AC0000000",
          "goodStanding": true,
          "addresses": [
            {
              "streetAddress": "123 Main St",
              "country": {"value": "CA", "text": "Canada"},
              "province": {"value": "ON", text": "Ontario"},
              "city": "Toronto",
              "postalCode": "M5V1E3",
              "index": 0
            },
            {
              "streetAddress": "456 Queen St",
              "country": {"value": "CA", text": "Canada"},
              "province": {"value": "QC", text": "Quebec"},
              "city": "Montreal",
              "postalCode": "H3B1A7",
              "index": 1,
              "contacts":{
                  "type": "person",
                  "firstName": "JAMES",
                  "lastName": "BAXTER",
                  "phoneNumber": "123456789"
                  "email": "james@email.ca",
              }
            }
          ]
        }"""
)
@With
public record ClientDetailsDto(
    @Schema(description = "The client name as registered on the BC Registry", example = "Acme Inc.")
    String name,
    @Schema(description = "ID of the client", example = "AC0000000")
    String id,
    @Schema(
        description = "Defines if the client is in good standing with the Ministry of Finance",
        example = "true"
    )
    boolean goodStanding,
    @Schema(description = "All available addresses", example = """
        [
          {
            "streetAddress": "123 Main St",
            "country": {"value": "CA", "text": "Canada"},
            "province": {"value": "ON", text": "Ontario"},
            "city": "Toronto",
            "postalCode": "M5V1E3",
            "index": 0
          },
          {
            "streetAddress": "456 Queen St",
            "country": {"value": "CA", text": "Canada"},
            "province": {"value": "QC", text": "Quebec"},
            "city": "Montreal",
            "postalCode": "H3B1A7",
            "index": 1
          }
        ]""")
    List<ClientAddressDto> addresses
) {
}
