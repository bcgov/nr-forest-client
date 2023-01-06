package ca.bc.gov.app.dto.ches;

import java.util.List;

public record ChesMailErrorResponse(
    String type,
    String title,
    int status,
    String detail,
    List<ChesMailError> errors
) {
}
