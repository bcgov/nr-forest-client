package ca.bc.gov.app.dto.orgbook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "NameListResponse", description = "A list of name results")
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrgBookResultListResponse(
    @Schema(description = "The total amount of entries on all pages", example = "75")
    int total,
    @Schema(description = "The zero-index current page number", example = "3")
    Integer page,

    @Schema(description = "The list of named results")
    List<OrgBookNameDto> results
) {
}