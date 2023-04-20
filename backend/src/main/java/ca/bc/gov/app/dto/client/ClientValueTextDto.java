package ca.bc.gov.app.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "A simple text and value object",
    title = "NameCode",
    example = """
        {
          "value": "00000002",
          "text": "BAXTER"
        }"""
)
public record ClientValueTextDto(

    @Schema(description = "The value for that specific object", example = "00000002")
    String value,

    @Schema(description = "The textual information for that specific object", example = "BAXTER")
    String text
) {
}
