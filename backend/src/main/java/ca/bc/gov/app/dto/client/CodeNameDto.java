package ca.bc.gov.app.dto.client;

/**
 * The {@code CodeNameDto} class represents a simple data transfer object (DTO) that encapsulates a
 * name and code for a specific object. It is used to transfer this information between different
 * parts of an application. This class is annotated with Swagger annotations for generating API
 * documentation. It provides a description and example JSON representation for the object it
 * represents.
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
 */
public record CodeNameDto(
    String code,

    String name
) {

}
