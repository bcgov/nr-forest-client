package ca.bc.gov.app.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(
    description = "A simple client lookup response object",
    title = "ClientLookup",
    example = """
        {
        "code": "00000002",
        "name": "BAXTER",
        "status": "ACTIVE",
        "legalType": "SP"
        }"""
)
public record ClientLookUpDto(
    @Schema(description = "The code for that specific object", example = "00000002")
    String code,
    @Schema(description = "The name information for that specific object", example = "BAXTER")
    String name,
    @Schema(description = "The status of the client, could be ACTIVE or INACTIVE",
        example = "ACTIVE")
    String status,
    @Schema(description = "The legal type of the client", example = "SP")
    String legalType
) {
}
