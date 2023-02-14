package ca.bc.gov.app.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "A simple name and code object",
    title = "NameCode",
    example = """
        {
          "code": "00000002",
          "name": "BAXTER"
        }"""
)
public record ClientNameCodeDto(

    @Schema(description = "The code for that specific object", example = "00000002")
    String code,

    @Schema(description = "The name information for that specific object", example = "BAXTER")
    String name
) {
}
