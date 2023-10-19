package ca.bc.gov.app.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The {@code CodeNameDto} class represents a simple data transfer object (DTO) that encapsulates
 * a name and code for a specific object. It is used to transfer this information between different
 * parts of an application.
 *
 * This class is annotated with Swagger annotations for generating API documentation. It provides
 * a description and example JSON representation for the object it represents.
 *
 * <p>
 * Example JSON representation:
 * <pre>{@code
 * {
 *   "code": "00000002",
 *   "name": "BAXTER"
 * }
 * }</pre>
 * </p>
 *
 * @see Schema
 */
@Schema(
    description = "A simple name and code object",
    title = "NameCode",
    example = """
        {
          "code": "00000002",
          "name": "BAXTER"
        }"""
)
public record CodeNameDto(
    /**
     * The code for the specific object. It is a string value.
     *
     * @see Schema
     */
    @Schema(description = "The code for that specific object", example = "00000002")
    String code,

    /**
     * The name information for the specific object. It is a string value.
     *
     * @see Schema
     */
    @Schema(description = "The name information for that specific object", example = "BAXTER")
    String name
) {
}
