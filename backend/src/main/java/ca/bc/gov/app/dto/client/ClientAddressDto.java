package ca.bc.gov.app.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.With;

@Schema(
    description = "An address information",
    title = "ClientAddressData",
    example = """
        {
          "locationName": "Billing Address",
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
          "index": 1
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

    @Schema(description = """
        The location name of an address. Examples of location names include,
        but are not limited to, Mailing Address, Billing Address among others.""",
        example = "Billing Address")
    String locationName
) {
  public Map<String, Object> description() {

    final String indexFormatted = String.format("address.[%d]", index);

    return
        Map.of(indexFormatted,
            Map.of(
                "name", locationName,
                "address", streetAddress,
                "country", country.text(),
                "province", province.text(),
                "city", city,
                "postalCode", postalCode
            )
        );

  }
}
